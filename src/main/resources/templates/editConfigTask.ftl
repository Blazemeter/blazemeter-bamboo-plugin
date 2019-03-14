<script>
    [#if savedWsp??]
        var savedWsp = "${savedWsp}";
    [#else]
        var savedWsp = "";
    [/#if]

    [#if savedTest??]
        var savedTest = "${savedTest}";
    [#else]
        var savedTest = "";
    [/#if]

    var bzmTestMap = [];

    [#foreach key in wspMap.keySet()]
        var array = [];
        [#foreach id in wspMap.get(key).keySet()]
            var obj = {};
            obj.id = "${id}";
            obj.value = "${wspMap.get(key).get(id).replace("\"", "'")}";
            array.push(obj);
        [/#foreach]
        array.sort(customComparator);
        bzmTestMap["${key}"] = array;
    [/#foreach]

    function customComparator(a, b) {
        var valueA = a.value.toUpperCase(); // ignore upper and lowercase
        var valueB = b.value.toUpperCase(); // ignore upper and lowercase
        if (valueA < valueB) {
            return -1;
        }
        if (valueA > valueB) {
            return 1;
        }

        // names must be equal
        return 0;
    }

    function onChangedWorkspaceSelect(wsp) {
        if (wsp.value) {
            checkForEmptyValue(wsp);
            var list = bzmTestMap[wsp.value];

            var testsSel = document.getElementById("selectedtest");
            testsSel.options.length = 0;

            for (var i = 0; i < list.length; i++) {
                var option = new Option(list[i].value, list[i].id)
                testsSel.options[i] = option;
            }
        } else {
            wsp.prepend(new Option("Select Workspace", ""));
            wsp.value = "";
            var testsSel = document.getElementById("selectedtest");
            testsSel.prepend(new Option("No Workspace", ""));
            testsSel.value = "";
        }
    }

    function checkForEmptyValue(wsp) {
        for (var i = 0; i < wsp.options.length; i++) {
            if (wsp.options[i].value == "") {
                wsp.options[i] = null;
            }
        }
    }
</script>

[@ww.select labelKey='blazemeter.workspace'
            name='selectedWorkspace'
            list='workspaceList'
            toggle='true'
            onchange='onChangedWorkspaceSelect(this);'
/]
[@ww.select labelKey='blazemeter.test' name='selectedtest' list='testlist'/]



[@ww.textfield labelKey="blazemeter.config.jmeterProps" name="jmeter.properties" required='false'/]
[@ww.textarea labelKey="blazemeter.config.notes" name="notes" required='false'/]
[@ww.checkbox labelKey="blazemeter.config.jtl" name='jtl.report' toggle='true' description=''/]
[@ww.textfield labelKey="blazemeter.config.jtl.path" name="jtl.path" required='false'/]
[@ww.checkbox labelKey="blazemeter.config.junit" name='junit.report' toggle='true' description=''/]
[@ww.textfield labelKey="blazemeter.config.junit.path" name="junit.path" required='false'/]

<script>
    var wspSel = document.getElementById("selectedWorkspace");
    wspSel.value = savedWsp;
    onChangedWorkspaceSelect(wspSel);

    // select value in tests selection
    var testsSelect = document.getElementById("selectedtest");
    var isSelected = false;
    for (var i = 0; i < testsSelect.options.length; i++) {
        if (testsSelect.options[i].value == savedTest) {
            testsSelect.options[i].selected = true;
            isSelected = true;
        }
    }
    if (!isSelected && testsSelect.options.length > 0) {
        testsSelect.options[0].selected = true;
    }
</script>