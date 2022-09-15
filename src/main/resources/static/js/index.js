$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	//当鼠标点击发布帖子以后，发布帖子的对话框隐藏掉
	$("#publishModal").modal("hide");

	//获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();

	//发送异步的post请求
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title":title, "content":content},
		function (data) {
			//从服务器获取提示消息
			data  = $.parseJSON(data);
			//并放入到提示框里  默认提示框是不显示的
			$("#hintBody").text(data.msg);
			//显示提示框
			$("#hintModal").modal("show");
			//2s后自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//如果发布成功了 刷新页面
				if (data.code == 0){
					window.location.reload();
				}
			}, 2000);
		}
	);
}