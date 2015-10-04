package com.github.samuelbr.dbcheck;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private ResultInfoFormatter formatter = new ResultInfoFormatter();
	
    public MainServlet() {
        super();
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ApplicationContext applicationContext = ApplicationContext.getApplicationContext(getServletContext());
		
		if (!applicationContext.isActive()) {
			response.setStatus(HttpServletResponse.SC_SEE_OTHER);
			return;
		}
		
		ResultInfoRepository resultInfoRepository = applicationContext.getResultInfoRepository();
		
		String clearFlag = request.getHeader("X-Clear");
		
		List<ResultInfo> results = clearFlag == null 
				? resultInfoRepository.get()
				: resultInfoRepository.clearAndGet();
		
		response.getWriter().append(formatter.format(results));
	}

}
