<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta charset="UTF-8" />
<!-- <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">  -->
<title>TpServer Admin</title>
<link rel="shortcut icon" href="../favicon.ico">
<link rel="stylesheet" type="text/css" href="css/login.css" />
<link rel="stylesheet" type="text/css" href="css/style.css" />
<link rel="stylesheet" type="text/css" href="css/animate-custom.css" />
<SCRIPT LANGUAGE="JavaScript">
	
	function textCounter(field, cntfield, maxlimit) {
		if (field.value.length > maxlimit) // if too long...trim it!
			field.value = field.value.substring(0, maxlimit);
		else
			cntfield.value = maxlimit - field.value.length;
	}
//  End -->
</script>

<%
	String username = (String) session.getAttribute("username");
	if (username == null || username.trim().length() == 0) {
		response.sendRedirect("login.jsp");
	}
%>

</head>

<body>
	<div class="container">
		<!-- Codrops top bar -->
		<div class="codrops-top">
			<div class="clr"></div>
		</div>
		<!--/ Codrops top bar -->
		<header>
		<h1>Nimbler Admin</h1>
		</header>
		<section>
		<div id="twitter">
			<a class="hiddenanchor" id="totweet"></a>
			<div id="wrapper">
				<div id="login" class="animate form">
					<form action="AdminServlet" method="post" name="tweetForm">
						<input type="hidden" name="opcode" value="tweet"/> 
						<p>
						<h1>What's happening?</h1>
							<!-- <label for="tweetPost" class="uname" data-icon="u">What's happening? </label>  -->
							<textarea id="tweetArea" name="tweet" autofocus="autofocus" rows="4" cols="50" style="max-width:100%; min-width:100%" 
							onKeyDown="textCounter(document.tweetForm.tweetArea,document.tweetForm.remLen2,120)"
							onKeyUp="textCounter(document.tweetForm.tweetArea,document.tweetForm.remLen2,120)" ></textarea>
						</p>
						<p class="login button">
							<input readonly type="text" name="remLen2" size="3" maxlength="3" value="120" width="10px"/>
							<input type="submit" value="Tweet" />
						</p>
					</form>
					<form action="AdminServlet" method="post" name="graphTest">
						<input type="hidden" name="opcode" value="testGraph"/> 
						<p class="login button">
							<input type="submit" value="Test Graph" />
						</p>
					</form>
					<%
						String message = request.getParameter("message");
						if (message != null) {
					%>
					<div style="color: green; font: bold;"><%=message%></div>
					<%
						}
						String graphTestMessage = request.getParameter("graphTest");
						if (graphTestMessage != null) {
					%>
					<div style="color: green; font: bold;"><%=graphTestMessage%></div>
					<%
						}
					%>
				</div>
			</div>
		</div>
		</section>
	</div>
</body>
</html>