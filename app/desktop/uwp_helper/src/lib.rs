use std::ffi::{c_char, CStr};

use windows::{
    core::{
        Error,
        HSTRING,
        h,
        Result
    },
    Foundation::Uri,
    Security::Credentials::{PasswordCredential, PasswordVault},
    System::Launcher,
    Win32::Foundation::E_FAIL,
    Services::Store::StoreContext,
    Storage::ApplicationData
};

const RESOURCE: &HSTRING = h!("dev.schlaubi.tonbrett/api_token");
const USERNAME: &HSTRING = h!("_");

#[repr(C)]
pub struct StringResult {
    is_error: bool,
    length: usize,
    string: HSTRING,
}

#[tokio::main]
#[no_mangle]
pub async extern "C" fn request_msstore_auto_update() -> bool {
    let result = _request_msstore_auto_update().await;
     match result {
        Err(err) => {
            log::error!("Could not request msstore update: {}", err);
            true
        }
        _ => false,
    }
}

#[tokio::main]
#[no_mangle]
pub async unsafe extern "C" fn launch_uri(uri: *const c_char) {
    let uri_str = CStr::from_ptr(uri).to_str().unwrap();
    _launch_uri(uri_str).await.unwrap()
}

#[no_mangle]
pub unsafe extern "C" fn store_token(token: *const c_char) {
    let token_str = CStr::from_ptr(token).to_str().unwrap();
    _store_token(token_str).unwrap()
}

#[no_mangle]
pub extern "C" fn get_token() -> StringResult {
    _get_token().into()
}

#[no_mangle]
pub unsafe extern "C" fn copy_string_from_get_string_result_into_buffer(
    result: StringResult,
    buffer: *mut u16,
) {
    buffer.copy_from(result.string.as_ptr(), result.length)
}

#[no_mangle]
pub unsafe extern "C" fn get_temp_folder() -> StringResult {
    _get_temp_folder().into()
}

async fn _launch_uri(uri: &str) -> Result<()> {
    let actual_uri = Uri::CreateUri(&HSTRING::from(uri))?;

    let result = Launcher::LaunchUriAsync(&actual_uri)?.await?;

    result.then(|| {}).ok_or(Error::from(E_FAIL))
}

fn _store_token(token: &str) -> Result<()> {
    let vault = PasswordVault::new()?;
    let credential =
        PasswordCredential::CreatePasswordCredential(RESOURCE, USERNAME, &HSTRING::from(token))?;

    vault.Add(&credential)
}

fn _get_token() -> Result<HSTRING> {
    let vault = PasswordVault::new()?;
    let credential = vault.Retrieve(RESOURCE, USERNAME)?;

    credential.Password()
}

fn _get_temp_folder() -> Result<HSTRING> {
    ApplicationData::Current()?.TemporaryFolder()?.Path()
}

async fn _request_msstore_auto_update() -> Result<()> {
    let context = StoreContext::GetDefault()?;
    let updates =
        context.GetAppAndOptionalStorePackageUpdatesAsync()?.await?;

    if updates.Size()? > 0 {
        context
            .RequestDownloadAndInstallStorePackageUpdatesAsync(&updates)?
            .await
            .map(|_| ())
    } else {
        Ok(())
    }
}

impl From<Result<HSTRING>> for StringResult {
    fn from(value: Result<HSTRING>) -> Self {
        let (string, is_error) = match value {
            Ok(password) => (password, false),
            Err(error) => (error.message().into(), true),
        };

        StringResult {
            is_error,
            length: string.len(),
            string,
        }
    }
}
