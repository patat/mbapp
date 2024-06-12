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
    <body>
    <header>
        <div class="container">
            <h1>Movie Battle App</h1>
        </div>
    </header>
    <main>
        <#nested>
    </main>
    <footer>
        <div class="container">
            Let movies fight to be watched.
        </div>
    </footer>
    <script src="/static/scripts/main.js"></script>
    </body>
    </html>
</#macro>