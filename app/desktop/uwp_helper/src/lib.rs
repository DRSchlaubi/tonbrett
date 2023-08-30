use std::ffi::{c_char, CStr};

use windows::{
    core::Result,
    core::{Error, HSTRING},
    Foundation::Uri,
    Storage::ApplicationData,
    System::Launcher,
    Win32::Foundation::E_FAIL,
};

#[tokio::main]
#[no_mangle]
pub async unsafe extern "C" fn launch_uri(uri: *const c_char) {
    let uri_str = CStr::from_ptr(uri).to_str().unwrap();
    _launch_uri(uri_str).await.unwrap()
}

async fn _launch_uri(uri: &str) -> Result<()> {
    let actual_uri = Uri::CreateUri(&HSTRING::from(uri))?;

    let result = Launcher::LaunchUriAsync(&actual_uri)?.await?;

    result.then(|| {}).ok_or(Error::from(E_FAIL))
}

#[repr(C)]
pub struct AppDataRoamingResult {
    is_error: bool,
    length: usize,
    string: HSTRING,
}

#[no_mangle]
pub extern "C" fn get_app_data_roaming() -> AppDataRoamingResult {
    let (string, is_error) = ApplicationData::Current()
        .and_then(|ad| ad.RoamingFolder())
        .and_then(|sf| sf.Path())
        .map(|p| (p, false))
        .unwrap_or_else(|e| (e.message(), true));
    AppDataRoamingResult { is_error, length: string.len(), string }
}

#[no_mangle]
pub extern "C" fn copy_string_from_get_app_data_roaming_result_into_buffer(
    result: AppDataRoamingResult,
    buffer: *mut u16,
) {
    unsafe { buffer.copy_from(result.string.as_ptr(), result.length) }
}
