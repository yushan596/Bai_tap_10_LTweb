$(document).ready(function () {
  var token = localStorage.getItem('token');
  if (!token) { window.location.href = '/login'; return; }

  // Hien thi thong tin nguoi dung dang nhap thanh cong
  $.ajax({
    type: 'GET',
    url: '/users/me',
    dataType: 'json',
    contentType: 'application/json; charset=utf-8',
    beforeSend: function (xhr) {
      xhr.setRequestHeader('Authorization', 'Bearer ' + token);
    },
    success: function (data) {
      $('#profile').text(data.fullName);
      $('#images').attr('src', '/images/' + data.images);
    },
    error: function () {
      localStorage.removeItem('token');
      alert('Sorry, you are not logged in.');
      window.location.href = '/login';
    }
  });

  // Dang xuat
  $('#logout').click(function () {
    localStorage.clear();
    window.location.href = '/login';
  });
});
