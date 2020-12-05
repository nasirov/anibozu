<!DOCTYPE html>
<html lang="en">
<head>
<#if errorMsg??>
  <title>${errorMsg}</title>
<#else>
  <title>Error</title>
</#if>
  <link rel="stylesheet" type="text/css" href="/css/center-style.css"/>
  <link rel="stylesheet" type="text/css" href="/css/style.css"/>
  <link rel="stylesheet" type="text/css" href="/css/nes.min.css"/>
  <link rel="stylesheet" type="text/css" href="/css/ps2p.css"/>
  <link rel="shortcut icon" type="image/x-icon" href="/img/faviconError.ico"/>
</head>
<body>
<div id="container">
  <div class="inner">
  <#if errorMsg??>
    <h2>${errorMsg}</h2>
  <#else>
    <h2>Sorry, something went wrong</h2>
  </#if>
    <span>
            <a href="/">
                <button type="button" class="nes-btn is-primary width_full">Home</button>
            </a>
        </span>
  </div>
</div>
</body>
</html>