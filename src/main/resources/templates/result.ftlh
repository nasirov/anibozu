<!DOCTYPE html>
<html lang="en">
<head>
  <title>Result for ${username}</title>
  <link rel="stylesheet" type="text/css" href="/css/style.css"/>
  <link rel="stylesheet" type="text/css" href="/css/ps2p.css"/>
  <link rel="stylesheet" type="text/css" href="/css/nes.min.css"/>
  <link rel="shortcut icon" type="image/x-icon" href="/img/favicon.ico"/>
  <meta charset="UTF-8">
</head>
<body>
<input id="username" type="hidden" value="${username}"/>
<input id="fandubList" type="hidden" value="${fandubList}"/>
<input id="watchingTitlesSize" type="hidden" value="${watchingTitlesSize}"/>
<header>
  <h1 id="status" class="is_centered">Processing result for ${username} ...</h1>
</header>
</body>
<script src="/js/jquery.min.js"></script>
<script src="/js/sse.js"></script>
</html>