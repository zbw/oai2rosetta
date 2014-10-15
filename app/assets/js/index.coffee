$ ->
  $.get "/repositories", (repositories) ->
    $.each repositories, (index, repository) ->
      $('#repositories').append $("
      <tr>
        <td>#{repository.title}</td>
        <td> #{repository.id}</td>
        <td><a href='/repository/#{repository.id}'> edit</a></td>
        <td></td>
        <td><a href='/record/list/#{repository.id}'> show</a></td>
      </tr>")