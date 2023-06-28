use windows::{
    core::Result,
    Storage::ApplicationData
};

fn main() -> Result<()> {
    let folder = ApplicationData::Current()?.RoamingFolder()?.Path()?;
    print!("{}", folder);
    Ok(())
}
