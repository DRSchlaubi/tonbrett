use std::ffi::{c_char, CStr};

use windows::Services::Store::StoreContext;
use windows::{
    core::h,
    core::Result,
    core::{Error, HSTRING},
    Foundation::Uri,
    Security::Credentials::{PasswordCredential, PasswordVault},
    System::Launcher,
    Win32::Foundation::E_FAIL,
};

const RESOURCE: &HSTRING = h!("dev.schlaubi.tonbrett/api_token");
const USERNAME: &HSTRING = h!("_");

#[tokio::main]
#[no_mangle]
pub async extern "C" fn request_msstore_auto_update() -> bool {
    let result = _request_msstore_auto_update().await;
    return match result {
        Err(err) => {
            log::error!("Could not request msstore update: {}", err);
            true
        }
        _ => false,
    };
}

async fn _request_msstore_auto_update() -> Result<()> {
    let context = StoreContext::GetDefault()?;
    let updates = context.GetAppAndOptionalStorePackageUpdatesAsync()?.await?;

    if updates.Size()? > 0 {
        context
            .RequestDownloadAndInstallStorePackageUpdatesAsync(&updates)?
            .await
            .map(|_| ())
    } else {
        Ok(())
    }
}

#[tokio::main]
#[no_mangle]
pub async unsafe extern "C" fn launch_uri(uri: *const c_char) {
    let uri_str = CStr::from_ptr(uri).to_str().unwrap();
    _launch_uri(uri_str).await.unwrap()
}

#[repr(C)]
pub struct StringResult {
    is_error: bool,
    length: usize,
    string: HSTRING,
}

#[no_mangle]
pub unsafe extern "C" fn store_token(token: *const c_char) {
    let token_str = CStr::from_ptr(token).to_str().unwrap();
    _store_token(token_str).unwrap()
}

#[no_mangle]
pub extern "C" fn get_token() -> StringResult {
    let result = _get_token();
    let (string, is_error) = match result {
        Ok(password) => (password, false),
        Err(error) => (error.message(), true),
    };

    StringResult {
        is_error,
        length: string.len(),
        string,
    }
}

#[no_mangle]
pub unsafe extern "C" fn copy_string_from_get_string_result_into_buffer(
    result: StringResult,
    buffer: *mut u16,
) {
    buffer.copy_from(result.string.as_ptr(), result.length)
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
    let password = credential.Password()?;

    Ok(password)
}
