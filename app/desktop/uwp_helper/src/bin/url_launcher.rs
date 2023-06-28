use std::ops::Not;
use clap::Parser;
use windows::{
    core::Result,
    Foundation::Uri,
    System::Launcher,
    core::{Error, HSTRING},
    Win32::Foundation::E_FAIL
};

#[derive(Parser, Debug)]
#[command(author, version, about, long_about = "Launches URL using UWP APIs")]
struct Args {
    // Uri
    #[arg(short, long)]
    uri: String,
}

#[tokio::main]
async fn main() -> Result<()> {
    let args = Args::parse();

    let uri = Uri::CreateUri(&HSTRING::from(args.uri))?;
    let result = Launcher::LaunchUriAsync(&uri)?.await?;

    result.then(|| ()).ok_or(Error::from(E_FAIL))
}
