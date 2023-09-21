param (
    [Parameter(Mandatory=$true)][String]$Version,
    [Parameter(Mandatory=$false)][String]$IsMsix = "false"
)

$file = 'appxmanifest.xml'
$xml = [System.Xml.XmlDocument]::new()
$xml.Load($file)

$identity = $xml.GetElementsByTagName("Identity")[0]
$identity.SetAttribute("Version", $Version)

if ($IsMsix.Equals("true"))
{
    $identity.SetAttribute("Name", "11839Schlaubi.dev.Tonbrett")
    $identity.SetAttribute("Publisher", "CN=4C05965C-D683-4386-B189-07E2E08CAA6C")

    $displayName = $xml.GetElementsByTagName("PublisherDisplayName")[0]
    $displayName.InnerText = "Schlaubi.dev"
}

$xml.Save($file)
