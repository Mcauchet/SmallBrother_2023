<#macro header>
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <link rel="stylesheet" type="text/css" href="index.css">
        <title>SmallBrother Admin Panel</title>
    </head>
    <style>
        * {
            box-sizing: border-box;
        }
        img {
            width: 10%;
            float: left;
        }
        .header {
            background-color: #f1f1f1;
            padding: 30px;
            text-align: center;
            font-size: 35px;
            width: 90%;
            float: left;
        }
    </style>
    <body style="text-align: center; font-family: sans-serif">
    <img src="/root/smallbrother/images/logo.png" alt="Logo">
    <div class="header" style="background-color: #f1f1f1;padding: 30px;text-align: center;font-size: 35px;">
        SmallBrother Admin Panel
    </div>
    <hr>
    <#nested>
    </body>
    </html>
</#macro>