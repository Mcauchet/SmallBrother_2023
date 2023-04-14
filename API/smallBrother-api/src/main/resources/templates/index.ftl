<#-- @ftlvariable name="aideDatas" type="kotlin.collections.List<com.example.models.AideData>" -->
<#import "_layout.ftl" as layout />

<style>
        ul {
            list-style: none;
            margin: 0;
            padding: 0;
            font-size: 18px;
        }

        li {
            padding: 10px 0;
            border-bottom: 1px solid #ccc;
        }

        a {
            text-decoration: none;
            color: #007bff;
        }

        form {
            display: flex;
            justify-content: center;
            align-items: center;
            margin-top: 20px;
        }

        input[type="submit"] {
            background-color: #55ab26;
            color: #fff;
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            margin-left: 10px;
        }

        @media (max-width: 600px) {
            .column {
                width: 100%;
            }
        }
</style>

<@layout.header>
    <div style="display: flex; justify-content: flex-end; align-items: center; margin-bottom: 20px;">
        <a href="/admin/editAdmin" style="margin-right: 20px;">Modifier le profil administrateur</a>
        <a href="/logout">Se déconnecter</a>
    </div>
    <#list aideDatas?reverse as aideData>
    <div class="column" style="width:50%; display: flex; justify-content: center; align-items: center;">
        <ul style="float: left;">
            <li><a href="/admin/${aideData.uri}">${aideData.uri}</a></li>
        </ul>
    </div>
    </#list>
    <hr>
    <div>
        <form action="/admin/clean" method="post">
            <p style="display: flex; justify-content: center">
                <input type="submit" name="_action" value="Clean"
                       style="flex: 0 0 auto; margin-right: 10px;">
                <input type="submit" name="_action" value="Delete All"
                       style="flex: 0 0 auto; margin-left: 10px;">
            </p>
        </form>
    </div>
</@layout.header>