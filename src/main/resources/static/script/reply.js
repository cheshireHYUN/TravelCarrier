// 댓글 모달창 활성화 - by윤아
$(".diary_viewport").on("click", ".d_slide > .reply_icon", function (e) {
  //var attachNo = $(this).closest(".d_slide").data("attachno");
  var attachNo = $(this).siblings("img").data("attachno");
  var currentImg = $(this).siblings("img").attr("src");
  currentReplyList(attachNo);
  $(".reply_img img").attr("src", currentImg);
  $(".reply_img img").attr("data-attachNo", attachNo);
  $(".reply_modal").addClass("show");
  $(".reply_inputText textarea").focus();
});

// 댓글 모달창 세로선 자동 생성 및 길이 수정
var reply_height = $(".reply_scroll").height();
console.log("댓글 스크롤 길이" + reply_height);
$(".reply_screen::before").css("height", (reply_height / 80) * 100 + "%");

//댓글모달창비활성화
$(".reply_modal .close").click(function () {
  $(".reply_modal").removeClass("show");
  $(".diary_noH .writing").show();
});

// 댓글리스트 새로고침 ajax- by.서현
function currentReplyList(attachNo) {
  $.ajax({
    type: "GET",
    url: "/TravelCarrier/reply/" + attachNo,
    dataType: "json",
    success: function (replyList) {
      console.log("댓글 List 로드 성공");
      appendReply(replyList);
    },
    error: function (jqXHR, textStatus, errorThrown) {
      alert("댓글 List 로드 실패");
    },
  });
}

// 댓글리스트 ajax후 append해주는 메소드 - by.서현
function appendReply(replyList) {
  // replyList : [{attachNo, cdate, origin, originName, replyId, text, thumbPath, udate, userId, userName, selfAuth},{}]
  $(".reply_scroll").empty();
  console.log(replyList);
  $.each(replyList, function (index, obj) {
    if (obj.udate == null) var date = obj.cdate;
    else var date = obj.udate + " (수정됨)";

    if (obj.origin == undefined || obj.origin == null || obj.origin == 0) {
      var html = newReplyHtml(
        obj.thumbPath,
        date,
        obj.userName,
        obj.text,
        obj.replyId,
        obj.ddate,
        obj.selfAuth
      );
      $(".reply_scroll").append(html);
      // $(".reply_input textarea").val("");
      $(".reply_inputText textarea").val("");
    } else {
      // 대댓글달기
      var targetDiv = $(`div[data-reply=${obj.origin}]`).parent();
      var recommentHtml = newReCommentHtml(
        obj.thumbPath,
        date,
        obj.userName,
        obj.text,
        obj.replyId,
        obj.originName,
        obj.ddate,
        obj.selfAuth
      );
      targetDiv.append(recommentHtml);
      $(".reply_inputText textarea").val("");
    }
  });
}

// 댓글 작성 이벤트 - by.서현
$(".reply_inputText button").on("click", function () {
  clickReplyBtn();
});
$("textarea").on("keydown", function () {
  if (event.keyCode === 13) {
    clickReplyBtn();
  }
});
function clickReplyBtn() {
  var type = $("div.tag_name span").eq(0).text();
  console.log(type);
  if (type == "댓글달기") createReply(type);
  else if (type == "댓글수정") modifyReply();
  else if (type.charAt(0) == "@") createReply(type);
  else alert("오류");
  // 댓글 작성/수정/대댓 후 인풋란 초기화
  $(".reply_input div.tag_name").html("<span>댓글달기</span>");
  $(".cancel").remove();
  $(".reply_input textarea").val("");
}

// 댓글 작성 유효성 검사 - by.서현
function validReply() {
  var comment = $(".reply_input textarea").val();
  if (comment == "") return false;
  else return true;
}

// 새 댓글 등록 ajax - by.서현
function createReply(type) {
  if (!validReply()) return;
/*    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");*/
  // 댓글이면 origin값이 "", 대댓글이면 origin값이 원댓글Id
  if (type == "댓글달기") var originVal = "";
  else if (type.charAt(0) == "@")
    var originVal = $("div.tag_name span.reply_num").text();
  // reply 필요조건 : ATTACH_NO, USER_ID, REPLY_TEXT, CDATE, UDATE, REPLY_ORIGIN
  var data = {
    attachNo: $(".reply_img img").attr("data-attachNo"),
    text: $(".reply_inputText textarea").val(),
    cdate: $.datepicker.formatDate("yy-mm-dd", new Date()),
    udate: null,
    origin: originVal,
  };
  console.log("데이터");
  console.log(data);
  $.ajax({
    type: "POST",
    url: "/TravelCarrier/reply/create",
    data: JSON.stringify(data),
    contentType: "application/json",
    success: function (resp) {
      console.log("성공");
      var attachNo = $(".reply_img img").attr("data-attachNo");
      currentReplyList(attachNo);


    },
    error: function (jqXHR, textStatus, errorThrown) {
      alert("댓글달기실패");
    },
  });
}


// 댓글 수정 ajax - by.서현
function modifyReply() {
  if (!validReply()) return;
  // reply 수정 : replyId, text, udate
  var data = {
    replyId: $(".tag_name span").eq(1).text(),
    text: $(".reply_input textarea").val(),
    udate: $.datepicker.formatDate("yy-mm-dd", new Date()),
  };
  console.log("수정");
  console.log(data);
  $.ajax({
    type: "POST",
    url: "/TravelCarrier/reply/modify",
    data: JSON.stringify(data),
    contentType: "application/json",
    success: function (replyId) {
      console.log("성공 " + replyId);
      var attachNo = $(".reply_img img").attr("data-attachNo");
      currentReplyList(attachNo);
    },
    error: function (jqXHR, textStatus, errorThrown) {
      alert("댓글수정실패");
    },
  });
}

// 댓글 append 틀 - by.서현
function newReplyHtml(img, date, name, comment, replyId, ddate, selfAuth) {
  var html;
  if (ddate != undefined || ddate != null) {
    html = `
        <div class="reply" >
          <div class="comment rep" data-reply="${replyId}">
            <div class="comment_date">
            </div>
            <div class="comment_textbox">
              <p class="comment_content">삭제된 댓글입니다.</p>
            </div>

          </div>
        </div>
        `;
  } else {
    html = `
    <div class="reply" >
      <div class="comment rep" data-reply="${replyId}">
        <div class="comment_profile">
          <div>
            <img src="${img}" alt="프로필사진">
          </div>
        </div>
        <div class="comment_date">
          <span>${date}</span>
        </div>
        <div class="comment_textbox">
          <span class="comment_id">${name}</span>
          <p class="comment_content">${comment}</p>
        </div>
        <div class="comment_btn">
          <span class="re_btn">답글달기</span>`;
        if (selfAuth == 1) {
          html += `<span class="mod_btn">수정하기</span>
          <span class="del_btn">삭제하기</span>`;
          }
        html += `
          </div>
      </div>
    </div>
    `;
  }
  return html;
}
// 대댓글
function newReCommentHtml(img,date,name,comment,replyId,originName,ddate,selfAuth) {
  var html;
  if (ddate != undefined || ddate != null) {
    html = `
      <div class="recomment rep" data-reply="${replyId}">
        <div class="comment_date">
        </div>
        <div class="comment_textbox">
          <p class="comment_content">
            삭제된 댓글입니다.
          </p>
        </div>
      </div>
    `;
  } else {
    html = `
      <div class="recomment rep" data-reply="${replyId}">
        <div class="comment_profile">
          <div>
            <img src="${img}" alt="프로필01">
          </div>
        </div>
        <div class="comment_date">
          <span>${date}</span>
        </div>
        <div class="comment_textbox">
          <span class="comment_id">${name}</span>
          <p class="comment_content">
            <span class="reply_id">@${originName}</span>
            ${comment}
          </p>
        </div>
        <div class="comment_btn">
          <span class="re_btn">답글달기</span>`;

    if (selfAuth == 1) {
          html+=
          `<span class="mod_btn">수정하기</span>
          <span class="del_btn">삭제하기</span>`;
      }
    html+=
        `</div>
      </div>`;
  }
  return html;
}
// 답글/수정 취소 이벤트 - by.서현
$(document).on("click", ".cancel", function (e) {
  $(".reply_input div.tag_name").html("<span>댓글달기</span>");
  $(".cancel").remove();
  $(".reply_inputText textarea").val("");
  $(".reply_inputText textarea").focus();
});
// 수정버튼 클릭 이벤트 - by.서현
$(document).on("click", ".mod_btn", function (e) {
  var $comment = $(this).closest(".rep");
  console.log($comment);
  $(".cancel").remove();
  $(".reply_inputText textarea").val("");
  $comment.find(".comment_btn").append("<span class='cancel'>수정취소</span>");
  var reply = $comment.data("reply");
  //var content = $comment.find(".comment_content").text();
  var content = $comment
    .find(".comment_content")
    .clone()
    .children()
    .remove()
    .end()
    .text()
    .trim();
  $(".tag_name").html(
    "<span>댓글수정</span> <span class=" + "reply_num" + ">" + reply + "</span>"
  );
  $(".reply_input textarea").val(content);
  $(".reply_inputText textarea").focus();
});

// 답글 작성 이벤트  - by.서현
$(document).on("click", ".re_btn", function (e) {
  var $comment = $(this).closest(".rep");
  $(".cancel").remove();
  $(".reply_input textarea").val("");
  $comment.find(".comment_btn").append("<span class='cancel'>답글취소</span>");
  var reply = $comment.data("reply");
  var name = $comment.find(".comment_id").text();
  $(".reply_input div.tag_name").html(
    "<span>@" +
      name +
      "</span>" +
      "<span class = " +
      "reply_num" +
      ">" +
      reply +
      "</span>"
  );
  $(".reply_inputText textarea").focus();
});

// 삭제 이벤트  - by.서현
$(document).on("click", ".del_btn", function (e) {
  var $comment = $(this).closest(".rep");
  var reply = $comment.data("reply");

  var data = {
    replyId: reply,
    ddate: $.datepicker.formatDate("yy-mm-dd", new Date()),
  };
  console.log("아이디");
  console.log(data);
  if (confirm(reply + "번 댓글을 삭제하시겠습니까?")) {
    $.ajax({
      type: "POST",
      url: "/TravelCarrier/reply/delete",
      data: JSON.stringify(data),
      contentType: "application/json",
      success: function (resp) {
        alert("삭제되었습니다.");
        var attachNo = $(".reply_img img").attr("data-attachNo");
        currentReplyList(attachNo);
      },
      error: function (jqXHR, textStatus, errorThrown) {
        alert("댓글 삭제 실패");
      },
    });
  }
});
