<#-- @ftlvariable name="aideDatas" type="kotlin.collections.List<com.example.models.AideData>" -->
<#import "_layout.ftl" as layout />
<@layout.header>
    <#list aideDatas?reverse as aideData>
    <div>
        <ul style="width:50%; float: left;">
            <li><a href="/admin/${aideData.uri}">${aideData.uri}</a></li>
        </ul>
    </div>
    </#list>
    <hr>
    <div>
        <form action="/admin/clean" method="post">
            <p style="display: flex; justify-content: center">
                <input type="submit" name="_action" value="Clean"
                       style="flex: 0 0 auto; margin-right: 10px; width: 5%;">
                <input type="submit" name="_action" value="Delete All"
                       style="flex: 0 0 auto; margin-right: 10px; width: 5%;">
            </p>
        </form>
    </div>
    <div>
        <a href="/admin/editAdmin">Edit Admin profile</a>
    </div>
    <div>
        <a href="/logout">Log out</a>
    </div>
</@layout.header>