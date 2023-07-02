param (
    [Parameter(Mandatory=$true)][string]$Version
)

$file = 'appxmanifest.xml'
$xml = [System.Xml.XmlDocument]::new()
$xml.Load($file)

$identity = $xml.GetElementsByTagName("Identity")[0]
$identity.SetAttribute("Version", $Version)

$xml.Save($file)
