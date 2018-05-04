<script>
    var savedWsp = "${savedWsp}";
    var savedTest = "${savedTest}";

    var bzmTestMap = [];

    [#foreach key in wspMap.keySet()]
        var array = [];
        [#foreach id in wspMap.get(key).keySet()]
            var obj = {};
            obj.id = "${id}";
            obj.value = "${wspMap.get(key).get(id)}";
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
        console.log(wsp.value);
        if (wsp.value) {
            var list = bzmTestMap[wsp.value];

            var testsSel = document.getElementById("selectedtest");
            testsSel.options.length = 0;

            for (var i = 0; i < list.length; i++) {
                var option = new Option(list[i].value, list[i].id)
                testsSel.options[i] = option;
            }
        // TODO: Add sort by label
        } else {
            wsp.prepend(new Option("Select Workspace", ""));
            wsp.value = "";
            var testsSel = document.getElementById("selectedtest");
            testsSel.prepend(new Option("No Workspace", ""));
            testsSel.value = "";
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
    console.log("opened");
    var wspSel = document.getElementById("selectedWorkspace");
    wspSel.value = savedWsp;
    onChangedWorkspaceSelect(wspSel);
    var testsSelect = document.getElementById("selectedtest");
    testsSelect.value = savedTest;
</script>