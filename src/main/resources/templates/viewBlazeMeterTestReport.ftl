<head>
    <meta name="tab" content="blazemeterJobReport"/>
</head>

<body>
<div>
	[#if isJob]
		[#if reportUrl?length gt 0]
             <script language='javascript' type='text/javascript'>
                 window.location.replace("${reportUrl}");
             </script>
        [#else]
			<p>BlazeMeter Test Report is not available for this job.</p>
		[/#if]
	[/#if]
</div>
</body>