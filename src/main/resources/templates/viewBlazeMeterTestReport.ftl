<head>
    <meta name="tab" content="blazemeterJobReport"/>
</head>

<body>
<div>
	[#if hasBzmReports]
	    [#if reports?? && (reports.size()>0)]

                [#foreach key in reports.keySet()]
            <div class="aui-message error">
                   <a href="${reports.get(key)}">BlazeMeter Report (Master #${key})</a>
            </div>
                [/#foreach]

		[#else]
			<p>BlazeMeter Test Reports not available for this job.</p>
		[/#if]
	[#else]
		<p>BlazeMeter Test Reports are available only for a specific job. Select the desired job number to view the test results.</p>
	[/#if]
</div>
</body>