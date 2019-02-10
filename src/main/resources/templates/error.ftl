<#import "/spring.ftl" as spring/>

<html>
<head>
    <title>Error</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css" />
    <link rel="shortcut icon" href="<@spring.url '/img/faviconError.ico'/>" type="image/x-icon">
</head>
<body>
<div class="container">
    <div class="jumbotron" >
        <h1 class="text-center"><i class="fa fa-frown-o"> </i>   Sorry, something went wrong. </h1>
        <p class="text-center"><a class="btn btn-primary" href="/"><i class="fa fa-home"></i>Try again</a></p>
    </div></div>
</body>
</html>