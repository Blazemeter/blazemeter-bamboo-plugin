<head>
    <meta name="tab" content="blazemeterJobReport"/>
</head>

<body>
<div>
    [#if reports?? && (reports.size()>0)]
        [#foreach key in reports.keySet()]
            <div class="aui-message">
                <a href="${reports.get(key)}">BlazeMeter Report (Master #${key})</a>
            </div>
        [/#foreach]
    [#else]
        <div class="aui-message warning">
            BlazeMeter Test Reports not available for this job.
        </div>
    [/#if]
</div>
</body>