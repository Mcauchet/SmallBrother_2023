<#-- @ftlvariable name="checkLog" type="Boolean" -->
<#import "_layout.ftl" as layout />
<style>
    * {
        box-sizing: border-box;
    }
    .column {
        text-align: center;
        padding: 10px;
    }

    form {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 10px;
    }

    label {
        width: 180px;
        display: inline-block;
    }

    input[type="text"],
    input[type="password"] {
        width: 200px;
        padding: 5px;
        border: 1px solid #ccc;
        border-radius: 5px;
    }

    input[type="submit"] {
        margin-top: 20px;
        background-color: #55ab26;
        color: #fff;
        padding: 15px 30px;
        font-size: large;
        border: none;
        border-radius: 5px;
        cursor: pointer;
    }

    @media (max-width: 600px) {
        .column {
            width: 100%;
        }
    }
</style>
<@layout.header>
    <div class="column">
        <form action="/login" method="post">
            <label for="email">Email:</label>
            <input type="text" id="email" name="email">
            <br><br>
            <label for="password">Mot de passe:</label>
            <input type="password" id="password" name="password">
            <br>
            <#if checkLog??>
            <#else>
                <p style="color: red;">L'adresse e-mail ou le mot de passe est incorrect</p>
            </#if>
            <br>
            <input style="alignment: right" type="submit" value="Login">
        </form>
    </div>
</@layout.header>
