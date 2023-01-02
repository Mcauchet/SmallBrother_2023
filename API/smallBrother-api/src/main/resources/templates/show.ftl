<#-- @ftlvariable name="aideData" type="com.example.models.AideData" -->
<#import "_layout.ftl" as layout />
<@layout.header>
    <div>
        <h3>
            ${aideData.uri}
        </h3>
        <p>
            ${aideData.aesKey}
        </p>
        <hr>
        <div>
            <form action ="/admin/${aideData.uri}" method="post">
                <p>
                    <input type="submit" name="_action" value="Delete">
                </p>
            </form>
        </div>
    </div>
    <div>
        <a href="/admin">Back to the main page</a>
    </div>
</@layout.header>