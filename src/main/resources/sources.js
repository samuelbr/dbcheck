{
	"DS1": [{
			"sql": "select count(*) as \"pocet\" from ITEM",
			"tags": ["items", "count"]
		}, {
			"sql": "select INVOICEID, COUNT(*) from ITEM GROUP BY INVOICEID",
			"tags": ["countByInvoiceId"]
		}],
	"DS2": [{
			"sql": "select count(*) from ITEM",
			"tags": ["items", "JNDI"]
	}]
}