package cat.iesesteveterradas.dbapi.endpoints;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cat.iesesteveterradas.dbapi.persistencia.Alojamiento;
import cat.iesesteveterradas.dbapi.persistencia.AlojamientoDao;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/editar/alojamiento")
public class EditarAlojamiento {
    private static final Logger logger = LoggerFactory.getLogger(PedirUnAlojamiento.class);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response editarAlojamiento(String jsonInput) {

        try {
            JSONObject input = new JSONObject(jsonInput);
            String alojamientoID = input.optString("alojamientoID", null);
            String descripcion = input.optString("descripcion", null);
            String nombre = input.optString("nombre", null);
            String direccion = input.optString("direccion", null);
            String capacidad = input.optString("capacidad", null);
            String reglas = input.optString("reglas", null);
            String precioPorNoche = input.optString("precioPorNoche", null);
            JSONArray urlFotosJson = input.optJSONArray("url");

            if (alojamientoID == null || alojamientoID.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"status\":\"ERROR\",\"message\":\"Alojamiento ID requerido\"}").build();
            }
            if (descripcion == null || descripcion.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"status\":\"ERROR\",\"message\":\"Descripción requerida\"}").build();
            }
            if (nombre == null || nombre.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"status\":\"ERROR\",\"message\":\"Nombre requerido\"}").build();
            }
            if (direccion == null || direccion.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"status\":\"ERROR\",\"message\":\"Dirección requerida\"}").build();
            }
            if (capacidad == null || capacidad.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"status\":\"ERROR\",\"message\":\"Capacidad requerida\"}").build();
            }
            if (reglas == null || reglas.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"status\":\"ERROR\",\"message\":\"Reglas requeridas\"}").build();
            }
            if (precioPorNoche == null || precioPorNoche.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"status\":\"ERROR\",\"message\":\"Precio por noche requerido\"}").build();
            }
            if (urlFotosJson == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"status\":\"ERROR\",\"message\":\"URL de foto requerida\"}").build();
            }

            Set<String> urlsExistentes = AlojamientoDao.obtenerUrlsPorAlojamientoID(Integer.parseInt(alojamientoID));
            Set<String> nuevasUrls = new HashSet<>();
            for (int i = 0; i < urlFotosJson.length(); i++) {
                nuevasUrls.add(urlFotosJson.getString(i));
            }

            // Determinar qué añadir y qué eliminar
            Set<String> urlsParaAgregar = new HashSet<>(nuevasUrls);
            urlsParaAgregar.removeAll(urlsExistentes);
            Set<String> urlsParaEliminar = new HashSet<>(urlsExistentes);
            urlsParaEliminar.removeAll(nuevasUrls);

            AlojamientoDao.eliminarUrlsFotos(Integer.parseInt(alojamientoID), urlsParaEliminar);
            AlojamientoDao.agregarUrlsFotos(Integer.parseInt(alojamientoID), urlsParaAgregar);

            AlojamientoDao.actualizarAlojamiento(Integer.parseInt(alojamientoID), descripcion, nombre, direccion,
                    capacidad, reglas, precioPorNoche);

            Alojamiento alojamiento = AlojamientoDao.encontrarAlojamientoPorId(Integer.parseInt(alojamientoID));

            JSONObject alojamientoJson = new JSONObject();
            alojamientoJson.put("nombre", alojamiento.getNombre());
            alojamientoJson.put("descripcion", alojamiento.getDescripcion());
            alojamientoJson.put("direccion", alojamiento.getDireccion());
            alojamientoJson.put("capacidad", alojamiento.getCapacidad());
            alojamientoJson.put("reglas", alojamiento.getReglas());
            alojamientoJson.put("precioPorNoche", alojamiento.getPrecioPorNoche());
            alojamientoJson.put("urlFoto", alojamiento.getUrlFotos());
            alojamientoJson.put("alojamientoID", alojamiento.getAlojamientoID());
            if (alojamiento.getPropietario() != null) {
                alojamientoJson.put("nombrePropietario", alojamiento.getPropietario().getNombre());
            } else {
                alojamientoJson.put("nombrePropietario", "No disponible");
            }

            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "OK");
            jsonResponse.put("message", "Datos de alojamientos obtenidos correctamente.");
            jsonResponse.put("data", alojamientoJson);

            return Response.ok(jsonResponse.toString(4)).build();
        } catch (Exception e) {
            logger.error("Error al obtener la información de los alojamientos", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Error al obtener la información de los alojamientos: " + e.getMessage());
            return Response.serverError().entity(errorResponse.toString()).build();
        }
    }
}
