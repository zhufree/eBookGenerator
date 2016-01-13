var myAudio = new Audio();
function play_sound(sound_url) {
    // if(myAudio.currentSrc!=sound_url){
    //     myAudio.pause();
    //     myAudio.setAttribute('src', sound_url);
    //     myAudio.load();
    //     myAudio.play();
    // }        
    // else{
    //     myAudio.setAttribute('src', sound_url);
    //     myAudio.load();
    //     myAudio.play();
    // }
    if(myAudio.paused==true){
        myAudio.setAttribute('src', sound_url);
        myAudio.load();
        myAudio.play();
        $('.play_logo').attr('src', '../static/img/stop.png');
        $('#playBtn').children().attr('src', '../static/img/stop.png');
    }        
    else{
        myAudio.pause();
        $('.play_logo').attr('src', '../static/img/play.png');
        $('#playBtn').children().attr('src', '../static/img/play.png');
    }
}
$(document).ready(function(){
    myAudio.setAttribute('src','');               
    myAudio.volume=0.5;        
    $('#playBtn').click(function(){
        if(myAudio.paused==true){
            myAudio.play();
            $(this).removeClass('play').addClass('pause');
            $(this).children().attr('src', '../static/img/stop.png');
            $('.play_logo').attr('src', '../static/img/stop.png');
        }        
        else{
            myAudio.pause();
            $(this).removeClass('pause').addClass('play');
            $(this).children().attr('src', '../static/img/play.png');
            $('.play_logo').attr('src', '../static/img/play.png');
        }
    });                                                        
       
    myAudio.addEventListener('timeupdate',function(){
        var total=parseInt(myAudio.duration);
        var ct=parseInt(myAudio.currentTime);
        $("#progress").slider("option",{max:total,value:ct});
        },false);

    $("#progress").slider({
        orientation: "horizontal",
        range: "min",
        max: 100,
        value: 0,
        animate:true,
        slide:function(event,ui){
            myAudio.pause();
            myAudio.currentTime=ui.value;
            $('#playBtn').children().attr('src', '../static/img/stop.png');
        },
        stop:function(event,ui){
            myAudio.play();                        
        }                                                        
    });        
                    
    $("#volume").slider({
         orientation: "vertical",
         range: "min",
         max: 100,
         value: 50,
         animate:true,
         slide:function(event,ui){
            var clsName=$("#volumeIco").attr("class");
            if(ui.value<50&&ui.value>0){
                $("#volumeIco").removeClass(clsName).addClass('volumeDown');
                $('#volumeIco').children().attr('src', '../static/img/volumedown.png');
                }
            if(ui.value>50){
                $("#volumeIco").removeClass(clsName).addClass('volumeUp');
                $('#volumeIco').children().attr('src', '../static/img/volumeup.png');
                }        
            if(ui.value==0){
                $("#volumeIco").removeClass(clsName).addClass('volumeOff');
                $('#volumeIco').children().attr('src', '../static/img/volumeoff.png');
                }        
            },
         stop:function(event,ui){
            myAudio.volume=ui.value/100;                        
         }                                                        
    });

});