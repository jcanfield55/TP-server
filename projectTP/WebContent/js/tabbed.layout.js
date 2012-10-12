jQuery(function($){

	tabs = function(options) {
		
		var defaults = {  
			selector: '.tabs',
			selectedClass: 'selected'
		};  
		
		if(typeof options == 'string') defaults.selector = options;
		var options = $.extend(defaults, options); 
	
		return $(options.selector).each(function(){
									
			var obj = this;	
			var targets = Array();
	
			function show(i){
				$.each(targets,function(index,value){
					$(value).hide();
				})
				$(targets[i]).fadeIn('fast');
				$(obj).children().removeClass(options.selectedClass);
				selected = $(obj).children().get(i);
				$(selected).addClass(options.selectedClass);
			};
	
			$('a',this).each(function(i){	
				targets.push($(this).attr('href'));
				$(this).click(function(e){
					e.preventDefault();
					show(i);
				});
			});
			
			show(0);
	
		});			
	}
	// initialize the function
	// as a parameter we are sending a selector. For this particular script we must select the unordered (or ordered) list item element 
	tabs('nav ul');

});

