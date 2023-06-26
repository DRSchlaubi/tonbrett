use windows::{
    core::Result,
    Storage::ApplicationData
};

#[tokio::main]
async fn main() -> Result<()> {
    let folder = ApplicationData::Current()?.RoamingFolder()?.Path()?;
    print!("{}", folder);
    Ok(())
}
