$ ->
  $('.jobbutton').click (ev)  ->
      ev.preventDefault()
      url = $(this).data('url')
      $.get ''+url,
        {},
        (msg) ->
          $('#statusmessage').html("Job started")
          $('#statusmessage').show()
          $('#statusmessage').fadeOut(1000)


