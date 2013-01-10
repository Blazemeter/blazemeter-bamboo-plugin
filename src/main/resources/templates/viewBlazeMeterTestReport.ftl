<head>
    [#if isJob]
        <meta name="tab" content="blazemeterJobReport"/>
    [#else]
        <meta name="tab" content="blazemeterJobReport"/>
    [/#if]
</head>

<body>
<div>
	[#if isJob]
		[#if sessionId?length gt 0]
			<iframe width="920" height="1500" src="https://a.blazemeter.com/report/${sessionId}/iframe">
			    <p>Your browser does not support iframes.</p>
			</iframe>
		[#else]
			<p>BlazeMeter Test Reports not available for this job.</p>
		[/#if]
	[#else]
		<p>BlazeMeter Test Reports are available only for a specific job. Select the desired job number to view the test results.</p>
	[/#if]
</div>
</body>