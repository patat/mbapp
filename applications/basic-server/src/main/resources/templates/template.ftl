<#macro noauthentication title="MBApp">
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name=viewport content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="/static/styles/reset.css">
        <link rel="stylesheet" href="/static/styles/style.css">
        <link rel="icon" type="image/svg" href="/static/images/favicon.svg">
        <title>Movie Battle App</title>
    </head>
    <body class="theme-dark">

    <header>
        <div class="logo-group">
            <p class="logo">MOVIE BATTLE</p>
            <img class="logo-dark" src="/static/images/logo_dark_full.svg" />
            <p class="sub-logo">Let movies fight to be watched</p>
        </div>
    </header>

    <main>
        <#nested>
    </main>

    <script src="/static/scripts/main.js"></script>
    </body>
    </html>
</#macro>