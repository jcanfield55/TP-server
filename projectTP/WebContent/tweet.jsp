<%@page import="com.nimbler.tp.util.PersistantHelper"%>
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
<script type="text/javascript" src="js/jquery.js"></script>
<SCRIPT LANGUAGE="JavaScript">
	function textCounter(field, cntfield, maxlimit) {
		if (field.value.length > maxlimit) // if too long...trim it!
			field.value = field.value.substring(0, maxlimit);
		else
			cntfield.value = maxlimit - field.value.length;
	}

	function validateForm() {
		var tweet = document.forms["tweetForm"]["tweet"].value;
		tweet = tweet.replace(/^\s+|\s+$/g, '');
		if (tweet == null || tweet == "") {
			alert("Please Enter Tweet.");
			return false;
		}
		
		var x = document.forms["tweetForm"]["deviceToken"].value;
		x = x.replace(/^\s+|\s+$/g, '');
		if (document.forms["tweetForm"]["chkSendToTestUser"].checked && (x == null || x == "")) {
			alert("Please Enter Device Token(s)");
			return false;
		}
	}
	
	$(document).ready(function() {
		$("input[name$='selectiontype']").click(function() {
			var test = $(this).val();
			$("div.desc").slideUp('slow');
			$("#seltype" + test).slideDown('slow');
			
		});
	});
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
					<form action="AdminServlet" method="post" name="tweetForm" onsubmit="return validateForm()">
					<div style="font: bold; font-size: 20px;" align="right">
							<a style="color: #1A5978;" href="logout.jsp">Logout</a>
					</div>
						<input type="hidden" name="opcode" value="tweet" />
						<p>
						<h1>What's happening?</h1>
						<!-- <label for="tweetPost" class="uname" data-icon="u">What's happening? </label>  -->
						<textarea id="tweetArea" name="tweet" autofocus="autofocus"
							rows="4" cols="50" style="max-width: 100%; min-width: 100%"
							onKeyDown="textCounter(document.tweetForm.tweetArea,document.tweetForm.remLen2,120)"
							onKeyUp="textCounter(document.tweetForm.tweetArea,document.tweetForm.remLen2,120)"></textarea>
						</p>
						<div width="100%">
							<div class="selectionradio" style="margin-bottom: 20px; width: auto;">
								<input type="radio" name="selectiontype" checked="checked" value="2" style="margin-bottom: 10px; width: auto;">Push To Test Users: </input>
								<div id="seltype2" class="desc" style="padding: 10px; border-left: .15em solid; background: #E9EAEE">
									<%
										PersistantHelper persistantHelper = new PersistantHelper();
										String deviceToken = persistantHelper.getTestUserDeviceTokens();
									%>

									<textarea id="deviceTokenArea" name="deviceToken" rows="4" cols="50" placeholder="Enter comma separated device tokens"
										style="max-width: 100%; min-width: 100%"><%=deviceToken%></textarea>
									<input type="radio" name="certificate" checked="checked" value="1" style="margin-top: 5px; width: auto;"> Nimbler Caltrain</input> <input
										type="radio" name="certificate" value="4" style="margin-top: 5px; width: auto;"> Nimbler SF</input><br /> <input align="left"
										name="chkEnableSound" type="checkbox" style="margin-top: 5px;">Enable Sound</input>
								</div>
							</div>
							<!-- -----------------------push by agency-------------------------- -->
							<div class="selectionradio" style="margin-bottom: 25px;">
								<input type="radio" name="selectiontype" value="3" align="left" style="width: auto;">Push By Agency:</input>
								<div id="seltype3" class="desc" style="display: none; padding: 10px; border-left: .15em solid; background: #E9EAEE">
									<input type="radio" name="agency" checked="checked" value="1" class="agencyradio" style="width: auto;"> Caltrain</input><br /> 
									<input type="radio" name="agency" value="2" class="agencyradio" style="width: auto;"> BART</input><br /> 
									<input type="radio" name="agency" value="3" class="agencyradio" style="width: auto;"> SFMTA</input><br /> 
									<input type="radio" name="agency" value="4" class="agencyradio" style="width: auto;"> Ac Transit</input>
								</div>
							</div>
							<!-- -----------------------push by App-------------------------- -->
							<div class="selectionradio" style="margin-bottom: 25px;">
								<input type="radio" name="selectiontype" value="4" align="left" style="width: auto;">Push By Application (Not as admin tweet): </input>
								<div id="seltype4" class="desc" style="display: none; padding: 10px; border-left: .15em solid; background: #E9EAEE">
									<input type="radio" name="apptype" value="1" checked="checked" class="agencyradio" style="width: auto;"> Nimbler Caltrain</input><br /> <input
										type="radio" name="apptype" value="4" class="agencyradio" style="width: auto;"> Nimbler SF</input><br />
								</div>
							</div>
						</div>


						<p class="login button">
							<input readonly type="text" name="remLen2" size="3" maxlength="3"
								value="120" width="10px" /> <input type="submit" value="Tweet" />
						</p>
					</form>
					<form action="AdminServlet" method="post" name="graphTest">
						<input type="hidden" name="opcode" value="testGraph" />
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