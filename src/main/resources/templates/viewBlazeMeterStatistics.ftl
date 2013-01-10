<html>
<head>
    <title>[@ui.header pageKey='BlazeMeter Test Reports' object='' title=true /]</title>
    <meta name="tab" content="blazemeterAllJobReport"/>
</head>

<body>
    <h1>BlazeMeter Statistics</h1>

    [#if chart??]
        <div>
            <div>
                <br/>
                <div class="fullyCentered">
                ${chart.imageMap}
<img id="chart" src="${req.contextPath}/chart?filename=${chart.location}" border="0" height="${chart.height}" width="${chart.width}" usemap="${chart.imageMapName}"/>
                </div>
            </div>              
        </div>
    [/#if]
</body>
</html> 