<script>
    function selectedWorkspace(wsp, all) {
        console.log(wsp.value);
        console.log(all);
    }

    var testList = "${testlist}";

    var bzmTestMap = [];

    [#foreach key in wspMap.keySet()]
        var array = [];
        [#foreach id in wspMap.get(key).keySet()]
            var obj = {};
            obj.id = "${id}";
            obj.value = "${wspMap.get(key).get(id)}";
            array.push(obj);
        [/#foreach]
        bzmTestMap["${key}"] = array;
    [/#foreach]

</script>

[@ww.select labelKey='blazemeter.workspace'
            name='selectedWorkspace'
            list='workspaceList'
            toggle='true'
            onchange='selectedWorkspace(this, bzmTestMap)'
/]
[@ww.select labelKey='blazemeter.test' name='selectedtest' list='testlist'/]



[@ww.textfield labelKey="blazemeter.config.jmeterProps" name="jmeter.properties" required='false'/]
[@ww.textarea labelKey="blazemeter.config.notes" name="notes" required='false'/]
[@ww.checkbox labelKey="blazemeter.config.jtl" name='jtl.report' toggle='true' description=''/]
[@ww.textfield labelKey="blazemeter.config.jtl.path" name="jtl.path" required='false'/]
[@ww.checkbox labelKey="blazemeter.config.junit" name='junit.report' toggle='true' description=''/]
[@ww.textfield labelKey="blazemeter.config.junit.path" name="junit.path" required='false'/]

<script>
    console.log("opened");
    var wspSel = document.getElementById("selectedWorkspace");
    selectedWorkspace(wspSel, bzmTestMap);
</script>