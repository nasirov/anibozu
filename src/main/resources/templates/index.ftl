<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Anime Checker</title>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/center-style.css')}"/>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/nes.min.css')}"/>
  <link rel="stylesheet" type="text/css" href="${versionedUrls.getForLookupPath('/css/ps2p.css')}"/>
  <link rel="shortcut icon" type="image/x-icon" href="${versionedUrls.getForLookupPath('/img/favicon.ico')}"/>
</head>

<body>
<div id="container">
  <div class="inner">
    <section class="nes-container with-title is-centered">
      <p class="title">Anime Checker</p>
      <div class="nes-field">
        <form id="username-submit-form" action="/result/" method="post">
          <input type="text" id="name_field" class="nes-input" name="username"
                 placeholder="Enter MAL username..."
                 title="Please enter a valid mal username  between 2 and 16 characters(latin letters, numbers, underscores and dashes only)"
                 autofocus>
          <button type="submit" class="nes-btn is-primary">Search for new episodes</button>
        </form>
      </div>
    </section>
  </div>
</div>
</body>
<script src="${versionedUrls.getForLookupPath('/js/jquery.min.js')}"></script>
<script src="${versionedUrls.getForLookupPath('/js/submit.form.js')}"></script>
</html>