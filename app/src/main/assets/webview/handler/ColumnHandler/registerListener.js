//尝试次数
let tryCount = 0;

//该函数开启一个定时任务，100ms后进行jQuery检查和监听器注册
function checkJquery() {
    setTimeout(function() {
        //检查$是否已定义
        try {
            $ == null;
        } catch(e) {
            //未定义，则重新执行此函数，重新开启定时任务
            tryCount++;
            //若已执行50次及以上（即已尝试5秒）则不再加载
            if(tryCount >= 50) return;
            //重新执行，本次不再继续向下执行
            checkJquery();
            return;
        }
        //注册元素添加监听器
        registerListener();
    }, 100);
}

//注册元素添加监听器
function registerListener() {
    $('div.read-reply').bind('DOMNodeInserted', function(e) {
        //本次添加的元素
        let elem = e.originalEvent.path[0];
        let className = $(elem).attr('class');
        //判断元素类型
        let content = null;
        //如果是一级回复
        if(className == 'reply-item') {
            let div = $(elem).find('div.content');
            if(div.length > 0) {
                //在有内容的情况下拿到评论内容
                content = div[0].innerText;
            }
        }
        //如果是二级回复
        else if(className == 'sub-preview-item') {
            let span = $(elem).find('span.content');
            if(span.length > 0) {
                let content = span[0].innerText;
                content = content.substring(1, content.length);
            }
        }
        //处理
        if(content == null) return;
        if(columnJsInterface.isBlockComment(content) == 'true') {
            console.log(content);
            $(elem).remove();
        }
    });
}

checkJquery();