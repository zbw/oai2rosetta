$ ->
  $.get "/repositories", (repositories) ->
    $.each repositories, (index, repository) ->
      $('#repositories').append $("
      <tr>
        <td>#{repository.title}</td>
        <td> #{repository.id}</td>
        <td><a href='/repository/#{repository.repository_id}'> edit</a></td>
        <td></td>
        <td><a href='/record/list/#{repository.repository_id}'> show</a></td>
      </tr>")