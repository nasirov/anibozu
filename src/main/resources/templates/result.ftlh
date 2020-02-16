<#import "available.ftlh" as available>
<#import "notAvailable.ftlh" as notAvailable>
<#import "notFound.ftlh" as notFound>
<!DOCTYPE html>
<html lang="en">
<head>
  <title>Result for ${username}</title>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/style.css')}"/>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/ps2p.css')}"/>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/nes.min.css')}"/>
  <link rel="shortcut icon" type="image/x-icon" href="${versionedUrls.getForLookupPath('/img/favicon.ico')}"/>
  <meta charset="UTF-8">
</head>
<body>
<#if resultAnimeList?has_content>
  <header>
    <h1>Result for ${username}</h1>
  </header>
    <@available.availableSection resultAnimeList fandubList/>
    <@notAvailable.notAvailableSection resultAnimeList fandubList/>
    <@notFound.notFoundSection resultAnimeList fandubList/>
</#if>
</body>
</html>