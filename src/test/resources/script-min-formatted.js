if($(document).ready(function(){$("form").on("submit",function(e){return""===$("#searchText").val()?($("#searchText").addClass("border-red"),e.preventDefault,!1):($("#searchText").removeClass("border-red"),!0)}),$(".fa.fa-search").click(function(e){""===$("#searchText").val()&&(e.preventDefault(),$("#searchText").addClass("border-red"))}),$("#submitLoadQuick").click(function(e){""===$("#searchTextQuick").val()?(e.preventDefault(),$("#searchTextQuick").addClass("border-red"),$("#resultsQuick").innerHTML("-")):$("#searchTextQuick").removeClass("border-red")}),

$("#submitLoadQuick").click(function(){
   var e=document.getElementById("searchTextQuick").value;
   $("#audioQuick").attr({src:"https://d1qx7pbj0dvboc_1.cloudfront.net/"+e+".mp3"});
   var n=document.getElementById("audioQuick");
   document.getElementById("audioQuick").volume=.8,
   n.play(),
   $("#resultsQuick").html(e),
   $(".modalResultQuick").css("display","block"),
   document.getElementById("searchTextQuick").value=""
});

document.location;$("button.hamburger").click(function(){$(this).toggleClass("is-active"),$(".mobile_menu").toggleClass("displayBlock")}),$(".alphContain ul li p").click(function(){$(this).find(".result_speaker").toggleClass("animate-speaker")}),

$(".alphContain ul li").click(function(){$(".result_speaker").removeClass("animate-speaker"),$(this).find(".result_speaker").addClass("animate-speaker")}),screen.width>560?

$(".alphContain p").click(function(){
    var e=$(this).text();
    $("#audio").attr({src:"https://d1qx7pbj0dvboc_2.cloudfront.net/"+e+".mp3"}),
    document.getElementById("audio").play()}):$(".alphContain p")
        .on("tap",function(){
            var e=$(this).text();
            $("#audio").attr({src:"https://d1qx7pbj0dvboc_3.cloudfront.net/"+e+".mp3"}),
            document.getElementById("audio").play()
        }),
    $(".removeTalk").click(function(){
        $(".quickTalk").removeClass("display-flex")}),
    $("button#quickTalkB").click(function(){$("#quickModal").css("display","block"),$(".quickTalk").toggleClass("display-flex")}),"block"==$("#quickModal").css("display")&&$("#searchTextQuick").focus(),$(".definitionView").click(function(){$(".definitionView").css("display","none")}),$("#crossOffThis").click(function(){$("#quickModal").css("display","none"),$(".quickTalk").toggleClass("display-flex")}),$("#defineQuick").click(function(){var e=document.getElementById("resultsQuick").innerHTML;window.open("https://www.google.co.th/search?q="+e+"+definition&oq=people+definition&gs_l=psy-ab.3..0.378719.381247.0.381438.17.15.0.0.0.0.252.1778.0j9j2.11.0....0...1.1.64.psy- ab..6.11.1778...0i131k1j0i67k1.OFErzmebQyg")}),$("#translateQuick").click(function(){var e=document.getElementById("resultsQuick").innerHTML;window.open("https://translate.google.com/#auto/zh-CN/"+e)}),

$(".modalResultQuick a").click(function(){
   var e=$(this).text();
   $("#audioQuick").attr({src:"https://d1qx7pbj0dvboc_4.cloudfront.net/"+e+".mp3"});
   var n=document.getElementById("audioQuick");
   document.getElementById("audioQuick").volume=.8,n.play()
}),

document.getElementById("searchTextQuick").value,$.fn.enterKey=function(e){return this.each(function(){$(this).keypress(function(n){"13"==(n.keyCode?n.keyCode:n.which)&&e.call(this,n)})})},

$("#searchTextQuick").enterKey(function(){
    var e=document.getElementById("searchTextQuick").value;
    if(""===e) $(this).addClass("border-red"); else {
        $(this).removeClass("border-red"),
        $(".loader").css("display","block"),
        $("#audioQuick").attr({src:"https://d1qx7pbj0dvboc_5.cloudfront.net/"+e+".mp3"});
        var n=document.getElementById("audioQuick");
        document.getElementById("audioQuick").volume=.8,n.play(),
        $("#resultsQuick").html(e),
        $(".modalResultQuick").css("display","block"),
        document.getElementById("searchTextQuick").value=""
    }
}),

$(window).width()<1100&&$("#hoverResult").on("tap",function(){var e=document.getElementById("searchText").value;

$("#audio").attr({src:"https://d1qx7pbj0dvboc_6.cloudfront.net/"+e+".mp3"});

var n=document.getElementById("audio");document.getElementById("audio").volume=.8,n.play()})}),$("#fourClose").click(function(){$(".adFourModal").slideUp(100)}),$("#submitLoad").click(function(){$(".loader").css("display","block"),setTimeout(function(){$(".loader").css("display","none")},1300)}),$(window).width()<1100&&$("#hoverResult").on("tap",function(){var e=document.getElementById("searchText").value;

$("#audio").attr({src:"https://d1qx7pbj0dvboc_7.cloudfront.net/"+e+".mp3"});

var n=document.getElementById("audio");document.getElementById("audio").volume=.8,n.play()}),"undefined"!=typeof Storage){$("#sound_on").change(function(){!0===$(this).prop("checked")&&($("#on_off").text("ON"),localStorage.setItem("sound_profile","sound_on")),!1===$(this).prop("checked")&&(localStorage.setItem("sound_profile","sound_off"),$("#on_off").text("OFF"))}),!0===$("#sound_on").prop("checked")&&localStorage.setItem("sound_profile","sound_on");var g=localStorage.getItem("sound_profile");if("sound_on"===g){$("#on_off").text("ON"),$("#sound_on").prop("checked",!0);for(var searchResult=$("#searchText").val(),wordarray=searchResult.split(";"),arrayLength=wordarray.length,i=0;i<arrayLength;i++){var j="<audio class='audio_play' id='"+wordarray[i]+"' src='https://d1qx7pbj0dvboc.cloudfront.net/"+wordarray[i]+".mp3'></audio>";$("body").append(j)}var arrayOfIds=$.map($(".audio_play"),function(e,n){return e.id}),sound1=document.getElementById(arrayOfIds[0]),sound2=document.getElementById(arrayOfIds[1]),sound3=document.getElementById(arrayOfIds[2]),sound4=document.getElementById(arrayOfIds[3]),sound5=document.getElementById(arrayOfIds[4]),sound6=document.getElementById(arrayOfIds[5]),sound7=document.getElementById(arrayOfIds[6]),sound8=document.getElementById(arrayOfIds[7]),sound9=document.getElementById(arrayOfIds[8]),sound10=document.getElementById(arrayOfIds[9]),sound11=document.getElementById(arrayOfIds[10]),sound12=document.getElementById(arrayOfIds[11]),sound13=document.getElementById(arrayOfIds[12]),sound14=document.getElementById(arrayOfIds[13]),sound15=document.getElementById(arrayOfIds[14]);$(".audio_play").prop("id").length>0&&(sound1.play(),sound1.onended=function(e){null==sound2?e.preventDefault():sound2.play()},sound2.onended=function(e){null==sound3?e.preventDefault():sound3.play()},sound3.onended=function(e){null==sound4?e.preventDefault():sound4.play()},sound4.onended=function(e){null==sound5?e.preventDefault():sound5.play()},sound5.onended=function(e){null==sound6?e.preventDefault():sound6.play()},sound6.onended=function(e){null==sound7?e.preventDefault():sound7.play()},sound7.onended=function(e){null==sound8?e.preventDefault():sound8.play()},sound8.onended=function(e){null==sound9?e.preventDefault():sound9.play()},sound9.onended=function(e){null==sound10?e.preventDefault():sound10.play()},sound10.onended=function(e){null==sound11?e.preventDefault():sound11.play()},sound11.onended=function(e){null==sound12?e.preventDefault():sound12.play()},sound12.onended=function(e){null==sound13?e.preventDefault():sound13.play()},sound13.onended=function(e){null==sound14?e.preventDefault():sound14.play()},sound14.onended=function(e){null==sound15?e.preventDefault():sound15.play()})}"sound_off"===g&&($("#sound_on").checked=!1,$("#on_off").text("OFF"))}else localStorage.setItem("sound_profile","sound_off"),$("#on_off").text("OFF");

