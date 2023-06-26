use clap::Parser;
use windows::{core::{Result}, Foundation::Uri, System::Launcher};
use windows::core::HSTRING;

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
    Launcher::LaunchUriAsync(&uri)?.await.map(|_| { () })
}
