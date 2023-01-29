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
<div class="flex-wrapper">
  <input id="username" type="hidden" value="${username}"/>
  <input id="fandubList" type="hidden" value="${fandubList}"/>
  <input id="titles" type="hidden" value="${titles}"/>
  <div class="container">
    <h1 id="status">${username}'s currently watching titles</h1>
    <section id="titles-section" class="titles"></section>
  </div>
</div>
</body>
<script src="/js/jquery.min.js"></script>
<script src="/js/result.js"></script>
</html>