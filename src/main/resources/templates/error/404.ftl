<#import "/spring.ftl" as spring/>

<html>
<head>
    <title>Not Found</title>
    <link rel="stylesheet" type="text/css" href="<@spring.url '/css/center-style.css'/>"/>
    <link rel="stylesheet" type="text/css" href="<@spring.url '/css/nes.min.css'/>"/>
    <link rel="stylesheet" type="text/css" href="<@spring.url '/css/ps2p.css'/>"/>
    <link rel="shortcut icon" type="image/x-icon" href="<@spring.url '/img/faviconError.ico'/>">
</head>
<body>
<div id="container">
    <div class="inner">
        <h2>Sorry, whatever you were looking for was not found</h2>
        <span>
            <a href="/">
                <button type="button" class="nes-btn is-primary">Home</button>
            </a>
        </span>
    </div>
</div>
</body>
</html>