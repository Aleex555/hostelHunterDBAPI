package cat.iesesteveterradas.dbapi.endpoints;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cat.iesesteveterradas.dbapi.persistencia.Alojamiento;
import cat.iesesteveterradas.dbapi.persistencia.AlojamientoDao;
import cat.iesesteveterradas.dbapi.persistencia.PropietarioDao;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/meter/alojamiento")
public class MeterAlojamiento {
    private static final Logger logger = LoggerFactory.getLogger(InformacionFlutter.class);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response informacionFlutter(String jsonInput) {

        try {
            JSONObject input = new JSONObject(jsonInput);
            String capacidad = input.optString("capacidad", null);
            String precio = input.optString("preciopornoche", null);
            String idpropietario = input.optString("id", null);
            String descripcion = input.optString("descripcion", null);
            String nombre = input.optString("nombre", null);
            String reglas = input.optString("reglas", null);
            JSONArray urlFotosJson = input.optJSONArray("url");
            String direccion = input.optString("direccion", null);

            if (capacidad == null || capacidad.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"status\":\"ERROR\",\"message\":\"capacidad requerido\"}").build();
            }
            if (precio == null || precio.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"status\":\"ERROR\",\"message\":\"precio requerido\"}").build();
            }
            if (idpropietario == null || idpropietario.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"status\":\"ERROR\",\"message\":\"Email requerido\"}").build();
            }
            if (descripcion == null || descripcion.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"status\":\"ERROR\",\"message\":\"descripcion requerido\"}").build();
            }
            if (nombre == null || nombre.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"status\":\"ERROR\",\"message\":\"nombre requerido\"}").build();
            }
            if (reglas == null || reglas.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"status\":\"ERROR\",\"message\":\"reglas requerido\"}").build();
            }

            if (urlFotosJson == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"status\":\"ERROR\",\"message\":\"urlFoto requerido\"}").build();
            }

            // Convertir JSONArray a Set<String>
            Set<String> urlFotos = new HashSet<>();
            for (int i = 0; i < urlFotosJson.length(); i++) {
                urlFotos.add(urlFotosJson.getString(i));
            }

            Alojamiento alojamiento = AlojamientoDao.crearAlojamiento(nombre, descripcion, direccion,
                    Integer.parseInt(capacidad), reglas, Double.parseDouble(precio), urlFotos, 0,
                    PropietarioDao.encontrarPropietarioPorID(Integer.parseInt(idpropietario)));
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "OK");
            jsonResponse.put("message", "Alojamiento creado correctamente");

            // Crear el objeto JSON para la parte "data"
            JSONObject userData = new JSONObject();
            userData.put("nombre", nombre);
            userData.put("Alojamiento", alojamiento.getAlojamientoID());

            // Añadir el objeto "data" al JSON de respuesta
            jsonResponse.put("data", userData);

            // Retorna la resposta
            String prettyJsonResponse = jsonResponse.toString(4); // 4 espais per indentar

            return Response.ok(prettyJsonResponse).build();
        } catch (Exception e) {
            logger.error("Error al meter un alojamiento", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Error al meter un alojamiento: " + e.getMessage());
            return Response.serverError().entity(errorResponse.toString()).build();
        }
    }
}
