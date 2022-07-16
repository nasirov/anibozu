<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Anime Checker</title>
  <link rel="stylesheet" type="text/css" href="/css/ps2p.css"/>
  <link rel="stylesheet" type="text/css" href="/css/nes.min.css"/>
  <link rel="stylesheet" type="text/css" href="/css/common-style.css"/>
  <link rel="shortcut icon" type="image/x-icon" href="/img/favicon.ico"/>
</head>

<body>
<div class="container">
  <div class="inner">
    <h1 class="is_centered">Anime Checker</h1>
    <section class="nes-container is-centered is-dark-alt">
      <div class="nes-field is-dark-alt">
        <form id="username-submit-form" action="/result" method="get">
          <input type="text" id="username" name="username" value="" class="nes-input is-dark-alt" placeholder="Enter MAL username..."
                 title="Please enter a valid mal username between 2 and 16 characters(latin letters, numbers, underscores and dashes only)" autofocus>
          <button type="submit" class="nes-btn is-dark-alt is-primary width_full">Search For New Episodes</button>
        </form>
      </div>
    </section>
  </div>
</div>
</body>
<script src="/js/jquery.min.js"></script>
<script src="/js/index.js"></script>
</html>