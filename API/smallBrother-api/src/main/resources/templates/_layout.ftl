<#macro header>
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <link rel="stylesheet" type="text/css" href="index.css">
        <title>Panneau d'administration - SmallBrother</title>
    </head>
    <style>
        * {
            box-sizing: border-box;
        }
    </style>
    <body style="text-align: center; font-family: sans-serif">
    <div class="header" style="background-color: #f1f1f1;padding: 30px;text-align: center;font-size: 35px;">
        Panneau d'administration - SmallBrother
    </div>
    <hr>
    <#nested>
    </body>
    </html>
</#macro>