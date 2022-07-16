<!DOCTYPE html>
<html lang="en">
<head>
  <title>Result for ${username}</title>
  <link rel="stylesheet" type="text/css" href="/css/ps2p.css"/>
  <link rel="stylesheet" type="text/css" href="/css/result-style.css"/>
  <link rel="shortcut icon" type="image/x-icon" href="/img/favicon.ico"/>
  <meta charset="UTF-8">
</head>
<body>
<div class="container result result--centered">
  <input id="username" type="hidden" value="${username}"/>
  <input id="fandubList" type="hidden" value="${fandubList}"/>
  <input id="titles" type="hidden" value="${titles}"/>
  <header class="result__header">
    <h1 id="status">Result for ${username}</h1>
  </header>
  <section id="titles-section" class="titles"></section>
</div>
</body>
<script src="/js/jquery.min.js"></script>
<script src="/js/result.js"></script>
</html>