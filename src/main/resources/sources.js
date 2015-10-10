{
	"jdbc/testDb": [{
			"sql": "select count(*) as \"pocet\" from INFORMATION_SCHEMA.TABLES",
			"tags": ["items", "count"]
		}, {
			"sql": "select * from OC_CL_INCIDENT_CATEGORY",
			"tags": ["countByInvoiceId"]
		}]
    /*
	,
	"DS2": [{
			"sql": "select count(*) from ITEM",
			"tags": ["items", "JNDI"]
	}]
	*/
}