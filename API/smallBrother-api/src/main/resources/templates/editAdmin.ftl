<#-- @ftlvariable name="confirm" type="Boolean" -->
<#-- @ftlvariable name="passwordsMatch" type="Boolean" -->
<#import "_layout.ftl" as layout />
<style>
    form {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 10px;
    }

    label {
        width: 280px;
        display: inline-block;
    }

    input[type="text"],
    input[type="password"] {
        width: 300px;
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

</style>
<@layout.header>
    <form action="/admin/editAdmin" method="post">
        <label for="email">Email:</label>
        <input type="text" id="email" name="email">
        <br>
        <label for="previousPassword">Mot de passe actuel:</label>
        <input type="password" id="previousPassword" name="previousPassword">
        <#if problemPwd??>
            <p style="color: red;">Le mot de passe est incorrect.</p>
        <#else>
        </#if>
        <br>
        <label for="newPassword">Nouveau mot de passe:</label>
        <input type="password" id="newPassword" name="newPassword">
        <br>
        <label for="confirmPassword">Confirmez le nouveau mot de passe:</label>
        <input type="password" id="confirmPassword" name="confirmPassword">
        <#if passwordsMatchError??>
            <p style="color: red;">Les nouveaux mots de passe ne correspondent pas.</p>
        <#else>
        </#if>
        <br>
        <label for="phone">Numéro de téléphone:</label>
        <input type="text" id="phone" name="phone">
        <br>
        <br>
        <input type="submit" value="Edit">
    </form>
</@layout.header>