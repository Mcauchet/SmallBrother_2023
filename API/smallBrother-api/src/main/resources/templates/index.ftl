<#-- @ftlvariable name="aideDatas" type="kotlin.collections.List<com.example.models.AideData>" -->
<#import "_layout.ftl" as layout />
<@layout.header>
    <#list aideDatas?reverse as aideData>
    <div>
        <h3>
            <a href="/admin/${aideData.uri}">${aideData.uri}</a>
        </h3>
        <p>
            ${aideData.aesKey}
        </p>
    </div>
    </#list>
    <hr>
    <div>
        <form action="/admin/clean" method="post">
            <p>
                <input type="submit" name="_action" value="Clean">
            </p>
        </form>
    </div>
</@layout.header>