#[macro_use]
extern crate lazy_static;

use std::ffi::{c_char, CStr};

use windows::{
    core::Result,
    core::{Error, HSTRING},
    Foundation::Uri,
    Storage::ApplicationData,
    System::Launcher,
    Win32::Foundation::E_FAIL,
};

lazy_static! {
    static ref APPDATA_FOLDER: Result<HSTRING> =
        ApplicationData::Current()?.RoamingFolder()?.Path();
}

#[tokio::main]
#[no_mangle]
pub async unsafe extern "C" fn launch_uri(uri: *const c_char) {
    let uri_str = CStr::from_ptr(uri).to_str().unwrap();
    _launch_uri(uri_str).await.unwrap()
}

#[no_mangle]
pub extern "C" fn get_appdata_folder_path_length() -> usize {
    APPDATA_FOLDER
        .as_ref()
        .map(|value| value.len())
        .unwrap_or_else(|error| {
            eprintln!("{}", error);
            0
        })
}

#[no_mangle]
pub unsafe extern "C" fn get_appdata_folder(buf: *mut u16) {
    let result = APPDATA_FOLDER.as_ref().unwrap();
    buf.copy_from(result.as_ptr(), result.len())
}

async fn _launch_uri(uri: &str) -> Result<()> {
    let actual_uri = Uri::CreateUri(&HSTRING::from(uri))?;

    let result = Launcher::LaunchUriAsync(&actual_uri)?.await?;

    result.then(|| {}).ok_or(Error::from(E_FAIL))
}
