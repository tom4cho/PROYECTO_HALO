package com.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.model.AptoDivisionEspecial;
import com.example.model.Division;
import com.example.model.DivisionEspecial;
import com.example.model.Rama;
import com.example.model.Recluta;
import com.example.service.CrudService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Gestión CRUD - UNSC")
@Route("")
public class MainView extends VerticalLayout {

    private final CrudService crudService;
    private final List<SerializableRunnable> comboRefrescos = new ArrayList<>();

    private static final String MSG_SIN_SELECCION = "Seleccione un registro de la tabla";
    private static final String MSG_NO_ELIMINABLE = "No se puede eliminar: tiene registros asociados";

    public MainView(CrudService crudService) {
        this.crudService = crudService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "white");

        TabSheet tabs = new TabSheet();
        tabs.setWidthFull();
        tabs.add("Ramas", crearSeccionRamas());
        tabs.add("Divisiones", crearSeccionDivisiones());
        tabs.add("Divisiones Especiales", crearSeccionDivisionesEspeciales());
        tabs.add("Reclutas", crearSeccionReclutas());
        tabs.add("Aptos División Especial", crearSeccionAptosDivisionEspecial());
        tabs.add("Consultas", crearSeccionConsultas());
        tabs.addSelectedChangeListener(e -> recargarCombos());

        add(new H2("Sistema UNSC - CRUD"), tabs);
    }

    // ==========================================
    // 1. RAMAS
    // ==========================================
    private Component crearSeccionRamas() {
        VerticalLayout layout = new VerticalLayout();
        TextField nombreField = new TextField("Nombre");
        TextField descripcionField = new TextField("Descripción");

        FormLayout form = new FormLayout(nombreField, descripcionField);

        Grid<Rama> grid = new Grid<>(Rama.class, false);
        grid.addColumn(Rama::getNombre).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(Rama::getDescripcion).setHeader("Descripción").setAutoWidth(true);

        Runnable refrescar = () -> grid.setItems(crudService.listarTodos(Rama.class));
        refrescar.run();

        grid.asSingleSelect().addValueChangeListener(e -> {
            Rama r = e.getValue();
            if (r != null) {
                nombreField.setValue(r.getNombre() != null ? r.getNombre() : "");
                descripcionField.setValue(r.getDescripcion() != null ? r.getDescripcion() : "");
            }
        });

        Button btnCrear = new Button("Crear", e -> {
            if (vacio(nombreField.getValue())) {
                Notification.show("Ingrese el nombre de la rama");
                return;
            }
            Rama r = new Rama(nombreField.getValue(), descripcionField.getValue());
            crudService.guardar(r);
            refrescar.run();
            limpiar(nombreField, descripcionField);
            grid.asSingleSelect().clear();
            Notification.show("Rama guardada");
        });
        btnCrear.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnActualizar = new Button("Actualizar", e -> {
            Rama r = grid.asSingleSelect().getValue();
            if (r == null) {
                Notification.show(MSG_SIN_SELECCION);
                return;
            }
            if (vacio(nombreField.getValue())) {
                Notification.show("Ingrese el nombre de la rama");
                return;
            }
            r.setNombre(nombreField.getValue());
            r.setDescripcion(descripcionField.getValue());
            crudService.actualizar(r);
            refrescar.run();
            Notification.show("Rama actualizada");
        });

        Button btnEliminar = new Button("Eliminar", e -> {
            Rama r = grid.asSingleSelect().getValue();
            if (r == null) {
                Notification.show(MSG_SIN_SELECCION);
                return;
            }
            try {
                crudService.eliminar(Rama.class, r.getIdRama());
                refrescar.run();
                limpiar(nombreField, descripcionField);
                grid.asSingleSelect().clear();
                Notification.show("Rama eliminada");
            } catch (Exception ex) {
                Notification.show(MSG_NO_ELIMINABLE);
            }
        });
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button btnLimpiar = new Button("Limpiar", e -> {
            limpiar(nombreField, descripcionField);
            grid.asSingleSelect().clear();
        });

        layout.add(form, crearAcciones(btnCrear, btnActualizar, btnEliminar, btnLimpiar), grid);
        return layout;
    }

    // ==========================================
    // 2. DIVISIONES
    // ==========================================
    private Component crearSeccionDivisiones() {
        VerticalLayout layout = new VerticalLayout();
        TextField nombreField = new TextField("Nombre");
        TextField descripcionField = new TextField("Descripción");
        ComboBox<Rama> ramaCombo = crearCombo("Rama", Rama.class, this::etiquetaRama);

        FormLayout form = new FormLayout(nombreField, descripcionField, ramaCombo);

        Grid<Division> grid = new Grid<>(Division.class, false);
        grid.addColumn(Division::getNombre).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(Division::getDescripcion).setHeader("Descripción").setAutoWidth(true);
        grid.addColumn(d -> d.getIdRama() != null ? d.getIdRama().getNombre() : "")
                .setHeader("Rama").setAutoWidth(true);

        Runnable refrescar = () -> grid.setItems(crudService.listarTodos(Division.class));
        refrescar.run();

        grid.asSingleSelect().addValueChangeListener(e -> {
            Division d = e.getValue();
            if (d != null) {
                nombreField.setValue(d.getNombre() != null ? d.getNombre() : "");
                descripcionField.setValue(d.getDescripcion() != null ? d.getDescripcion() : "");
                ramaCombo.setValue(d.getIdRama());
            }
        });

        Button btnCrear = new Button("Crear", e -> {
            if (vacio(nombreField.getValue())) {
                Notification.show("Ingrese el nombre de la división");
                return;
            }
            if (ramaCombo.getValue() == null) {
                Notification.show("Seleccione una rama");
                return;
            }
            Division d = new Division(nombreField.getValue(), descripcionField.getValue(), ramaCombo.getValue());
            crudService.guardar(d);
            refrescar.run();
            limpiar(nombreField, descripcionField, ramaCombo);
            grid.asSingleSelect().clear();
            Notification.show("División guardada");
        });
        btnCrear.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnActualizar = new Button("Actualizar", e -> {
            Division d = grid.asSingleSelect().getValue();
            if (d == null) {
                Notification.show(MSG_SIN_SELECCION);
                return;
            }
            if (vacio(nombreField.getValue())) {
                Notification.show("Ingrese el nombre de la división");
                return;
            }
            if (ramaCombo.getValue() == null) {
                Notification.show("Seleccione una rama");
                return;
            }
            d.setNombre(nombreField.getValue());
            d.setDescripcion(descripcionField.getValue());
            d.setIdRama(ramaCombo.getValue());
            crudService.actualizar(d);
            refrescar.run();
            Notification.show("División actualizada");
        });

        Button btnEliminar = new Button("Eliminar", e -> {
            Division d = grid.asSingleSelect().getValue();
            if (d == null) {
                Notification.show(MSG_SIN_SELECCION);
                return;
            }
            try {
                crudService.eliminar(Division.class, d.getIdDivision());
                refrescar.run();
                limpiar(nombreField, descripcionField, ramaCombo);
                grid.asSingleSelect().clear();
                Notification.show("División eliminada");
            } catch (Exception ex) {
                Notification.show(MSG_NO_ELIMINABLE);
            }
        });
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button btnLimpiar = new Button("Limpiar", e -> {
            limpiar(nombreField, descripcionField, ramaCombo);
            grid.asSingleSelect().clear();
        });

        layout.add(form, crearAcciones(btnCrear, btnActualizar, btnEliminar, btnLimpiar), grid);
        return layout;
    }

    // ==========================================
    // 3. DIVISIONES ESPECIALES
    // ==========================================
    private Component crearSeccionDivisionesEspeciales() {
        VerticalLayout layout = new VerticalLayout();
        TextField nombreField = new TextField("Nombre");
        TextField descripcionField = new TextField("Descripción");
        ComboBox<Division> divisionCombo = crearCombo("División", Division.class, this::etiquetaDivision);

        FormLayout form = new FormLayout(nombreField, descripcionField, divisionCombo);

        Grid<DivisionEspecial> grid = new Grid<>(DivisionEspecial.class, false);
        grid.addColumn(DivisionEspecial::getNombre).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(DivisionEspecial::getDescripcion).setHeader("Descripción").setAutoWidth(true);
        grid.addColumn(de -> de.getIdDivision() != null ? de.getIdDivision().getNombre() : "")
                .setHeader("División").setAutoWidth(true);

        Runnable refrescar = () -> grid.setItems(crudService.listarTodos(DivisionEspecial.class));
        refrescar.run();

        grid.asSingleSelect().addValueChangeListener(e -> {
            DivisionEspecial de = e.getValue();
            if (de != null) {
                nombreField.setValue(de.getNombre() != null ? de.getNombre() : "");
                descripcionField.setValue(de.getDescripcion() != null ? de.getDescripcion() : "");
                divisionCombo.setValue(de.getIdDivision());
            }
        });

        Button btnCrear = new Button("Crear", e -> {
            if (vacio(nombreField.getValue())) {
                Notification.show("Ingrese el nombre de la división especial");
                return;
            }
            if (divisionCombo.getValue() == null) {
                Notification.show("Seleccione una división");
                return;
            }
            DivisionEspecial de = new DivisionEspecial(nombreField.getValue(), descripcionField.getValue(),
                    divisionCombo.getValue());
            crudService.guardar(de);
            refrescar.run();
            limpiar(nombreField, descripcionField, divisionCombo);
            grid.asSingleSelect().clear();
            Notification.show("División especial guardada");
        });
        btnCrear.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnActualizar = new Button("Actualizar", e -> {
            DivisionEspecial de = grid.asSingleSelect().getValue();
            if (de == null) {
                Notification.show(MSG_SIN_SELECCION);
                return;
            }
            if (vacio(nombreField.getValue())) {
                Notification.show("Ingrese el nombre de la división especial");
                return;
            }
            if (divisionCombo.getValue() == null) {
                Notification.show("Seleccione una división");
                return;
            }
            de.setNombre(nombreField.getValue());
            de.setDescripcion(descripcionField.getValue());
            de.setIdDivision(divisionCombo.getValue());
            crudService.actualizar(de);
            refrescar.run();
            Notification.show("División especial actualizada");
        });

        Button btnEliminar = new Button("Eliminar", e -> {
            DivisionEspecial de = grid.asSingleSelect().getValue();
            if (de == null) {
                Notification.show(MSG_SIN_SELECCION);
                return;
            }
            try {
                crudService.eliminar(DivisionEspecial.class, de.getIdDivisionEspecial());
                refrescar.run();
                limpiar(nombreField, descripcionField, divisionCombo);
                grid.asSingleSelect().clear();
                Notification.show("División especial eliminada");
            } catch (Exception ex) {
                Notification.show(MSG_NO_ELIMINABLE);
            }
        });
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button btnLimpiar = new Button("Limpiar", e -> {
            limpiar(nombreField, descripcionField, divisionCombo);
            grid.asSingleSelect().clear();
        });

        layout.add(form, crearAcciones(btnCrear, btnActualizar, btnEliminar, btnLimpiar), grid);
        return layout;
    }

    // ==========================================
    // 4. RECLUTAS
    // ==========================================
    private Component crearSeccionReclutas() {
        VerticalLayout layout = new VerticalLayout();
        TextField nombreField = new TextField("Nombre");
        TextField apellidoField = new TextField("Apellido");
        IntegerField alturaField = new IntegerField("Altura");
        BigDecimalField pesoField = new BigDecimalField("Peso");
        ComboBox<Rama> ramaCombo = crearCombo("Rama", Rama.class, this::etiquetaRama);

        FormLayout form = new FormLayout(nombreField, apellidoField, alturaField, pesoField, ramaCombo);

        Grid<Recluta> grid = new Grid<>(Recluta.class, false);
        grid.addColumn(Recluta::getNombre).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(Recluta::getApellido).setHeader("Apellido").setAutoWidth(true);
        grid.addColumn(Recluta::getAltura).setHeader("Altura").setAutoWidth(true);
        grid.addColumn(Recluta::getPeso).setHeader("Peso").setAutoWidth(true);
        grid.addColumn(r -> r.getIdRama() != null ? r.getIdRama().getNombre() : "")
                .setHeader("Rama").setAutoWidth(true);
        grid.addColumn(r -> r.getIdApto() != null ? "Sí" : "No")
                .setHeader("Apto").setAutoWidth(true);

        Runnable refrescar = () -> grid.setItems(crudService.listarTodos(Recluta.class));
        refrescar.run();

        grid.asSingleSelect().addValueChangeListener(e -> {
            Recluta r = e.getValue();
            if (r != null) {
                nombreField.setValue(r.getNombre() != null ? r.getNombre() : "");
                apellidoField.setValue(r.getApellido() != null ? r.getApellido() : "");
                alturaField.setValue(r.getAltura());
                pesoField.setValue(r.getPeso());
                ramaCombo.setValue(r.getIdRama());
            }
        });

        Button btnCrear = new Button("Crear", e -> {
            if (vacio(nombreField.getValue())) {
                Notification.show("Ingrese el nombre del recluta");
                return;
            }
            if (vacio(apellidoField.getValue())) {
                Notification.show("Ingrese el apellido del recluta");
                return;
            }
            if (ramaCombo.getValue() == null) {
                Notification.show("Seleccione una rama");
                return;
            }
            Recluta r = new Recluta(nombreField.getValue(), apellidoField.getValue(), alturaField.getValue(),
                    pesoField.getValue(), ramaCombo.getValue(), null);
            crudService.guardar(r);
            refrescar.run();
            limpiar(nombreField, apellidoField, alturaField, pesoField, ramaCombo);
            grid.asSingleSelect().clear();
            Notification.show("Recluta guardado");
        });
        btnCrear.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnActualizar = new Button("Actualizar", e -> {
            Recluta r = grid.asSingleSelect().getValue();
            if (r == null) {
                Notification.show(MSG_SIN_SELECCION);
                return;
            }
            if (vacio(nombreField.getValue())) {
                Notification.show("Ingrese el nombre del recluta");
                return;
            }
            if (vacio(apellidoField.getValue())) {
                Notification.show("Ingrese el apellido del recluta");
                return;
            }
            if (ramaCombo.getValue() == null) {
                Notification.show("Seleccione una rama");
                return;
            }
            r.setNombre(nombreField.getValue());
            r.setApellido(apellidoField.getValue());
            r.setAltura(alturaField.getValue());
            r.setPeso(pesoField.getValue());
            r.setIdRama(ramaCombo.getValue());
            crudService.actualizar(r);
            refrescar.run();
            Notification.show("Recluta actualizado");
        });

        Button btnEliminar = new Button("Eliminar", e -> {
            Recluta r = grid.asSingleSelect().getValue();
            if (r == null) {
                Notification.show(MSG_SIN_SELECCION);
                return;
            }
            try {
                crudService.eliminar(Recluta.class, r.getIdRecluta());
                refrescar.run();
                limpiar(nombreField, apellidoField, alturaField, pesoField, ramaCombo);
                grid.asSingleSelect().clear();
                Notification.show("Recluta eliminado");
            } catch (Exception ex) {
                Notification.show(MSG_NO_ELIMINABLE);
            }
        });
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button btnLimpiar = new Button("Limpiar", e -> {
            limpiar(nombreField, apellidoField, alturaField, pesoField, ramaCombo);
            grid.asSingleSelect().clear();
        });

        layout.add(form, crearAcciones(btnCrear, btnActualizar, btnEliminar, btnLimpiar), grid);
        return layout;
    }

    // ==========================================
    // 5. APTOS DIVISIÓN ESPECIAL
    // ==========================================
    private Component crearSeccionAptosDivisionEspecial() {
        VerticalLayout layout = new VerticalLayout();
        ComboBox<Recluta> reclutaCombo = crearCombo("Recluta", Recluta.class, this::nombreCompleto);
        ComboBox<DivisionEspecial> divisionEspecialCombo = crearCombo("División Especial", DivisionEspecial.class,
                this::etiquetaDivisionEspecial);
        Checkbox aptoServicioField = new Checkbox("Apto para servicio");
        DatePicker fechaEvaluacionField = new DatePicker("Fecha de evaluación");
        IntegerField puntajeField = new IntegerField("Puntaje");

        FormLayout form = new FormLayout(reclutaCombo, divisionEspecialCombo, aptoServicioField,
                fechaEvaluacionField, puntajeField);

        Grid<AptoDivisionEspecial> grid = new Grid<>(AptoDivisionEspecial.class, false);
        grid.addColumn(a -> a.getIdRecluta() != null ? nombreCompleto(a.getIdRecluta()) : "")
                .setHeader("Recluta").setAutoWidth(true);
        grid.addColumn(a -> a.getIdDivisionEspecial() != null ? a.getIdDivisionEspecial().getNombre() : "")
                .setHeader("División Especial").setAutoWidth(true);
        grid.addColumn(AptoDivisionEspecial::getAptoServicio).setHeader("Apto Servicio").setAutoWidth(true);
        grid.addColumn(AptoDivisionEspecial::getFechaEvaluacion).setHeader("Fecha Evaluación").setAutoWidth(true);
        grid.addColumn(AptoDivisionEspecial::getPuntaje).setHeader("Puntaje").setAutoWidth(true);

        Runnable refrescar = () -> grid.setItems(crudService.listarTodos(AptoDivisionEspecial.class));
        refrescar.run();

        grid.asSingleSelect().addValueChangeListener(e -> {
            AptoDivisionEspecial a = e.getValue();
            if (a != null) {
                reclutaCombo.setValue(a.getIdRecluta());
                divisionEspecialCombo.setValue(a.getIdDivisionEspecial());
                aptoServicioField.setValue(Boolean.TRUE.equals(a.getAptoServicio()));
                fechaEvaluacionField.setValue(a.getFechaEvaluacion());
                puntajeField.setValue(a.getPuntaje());
            }
        });

        Button btnCrear = new Button("Crear", e -> {
            if (reclutaCombo.getValue() == null) {
                Notification.show("Seleccione un recluta");
                return;
            }
            if (divisionEspecialCombo.getValue() == null) {
                Notification.show("Seleccione una división especial");
                return;
            }
            try {
                AptoDivisionEspecial a = new AptoDivisionEspecial(reclutaCombo.getValue(),
                        divisionEspecialCombo.getValue(), aptoServicioField.getValue(),
                        fechaEvaluacionField.getValue(), puntajeField.getValue());
                crudService.guardar(a);
                refrescar.run();
                limpiar(reclutaCombo, divisionEspecialCombo, aptoServicioField, fechaEvaluacionField,
                        puntajeField);
                grid.asSingleSelect().clear();
                Notification.show("Apto guardado");
            } catch (Exception ex) {
                Notification.show("No se pudo guardar: el recluta ya tiene un apto asignado");
            }
        });
        btnCrear.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnActualizar = new Button("Actualizar", e -> {
            AptoDivisionEspecial a = grid.asSingleSelect().getValue();
            if (a == null) {
                Notification.show(MSG_SIN_SELECCION);
                return;
            }
            if (reclutaCombo.getValue() == null) {
                Notification.show("Seleccione un recluta");
                return;
            }
            if (divisionEspecialCombo.getValue() == null) {
                Notification.show("Seleccione una división especial");
                return;
            }
            a.setIdRecluta(reclutaCombo.getValue());
            a.setIdDivisionEspecial(divisionEspecialCombo.getValue());
            a.setAptoServicio(aptoServicioField.getValue());
            a.setFechaEvaluacion(fechaEvaluacionField.getValue());
            a.setPuntaje(puntajeField.getValue());
            crudService.actualizar(a);
            refrescar.run();
            Notification.show("Apto actualizado");
        });

        Button btnEliminar = new Button("Eliminar", e -> {
            AptoDivisionEspecial a = grid.asSingleSelect().getValue();
            if (a == null) {
                Notification.show(MSG_SIN_SELECCION);
                return;
            }
            try {
                crudService.eliminar(AptoDivisionEspecial.class, a.getIdApto());
                refrescar.run();
                limpiar(reclutaCombo, divisionEspecialCombo, aptoServicioField, fechaEvaluacionField,
                        puntajeField);
                grid.asSingleSelect().clear();
                Notification.show("Apto eliminado");
            } catch (Exception ex) {
                Notification.show(MSG_NO_ELIMINABLE);
            }
        });
        btnEliminar.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button btnLimpiar = new Button("Limpiar", e -> {
            limpiar(reclutaCombo, divisionEspecialCombo, aptoServicioField, fechaEvaluacionField,
                    puntajeField);
            grid.asSingleSelect().clear();
        });

        layout.add(form, crearAcciones(btnCrear, btnActualizar, btnEliminar, btnLimpiar), grid);
        return layout;
    }

    // ==========================================
    // 6. CONSULTAS DE ADSCRIPCIONES
    // ==========================================
    private Component crearSeccionConsultas() {
        VerticalLayout layout = new VerticalLayout();

        ComboBox<Division> divisionCombo = crearCombo("División", Division.class, this::etiquetaDivision);
        ComboBox<DivisionEspecial> divisionEspecialCombo = crearCombo("División Especial",
                DivisionEspecial.class, this::etiquetaDivisionEspecial);
        Checkbox soloAptosField = new Checkbox("Solo soldados aptos");

        divisionCombo.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                divisionEspecialCombo.clear();
            }
        });
        divisionEspecialCombo.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                divisionCombo.clear();
            }
            soloAptosField.setEnabled(e.getValue() == null);
        });

        Grid<Recluta> gridReclutas = new Grid<>(Recluta.class, false);
        gridReclutas.addColumn(Recluta::getNombre).setHeader("Nombre").setAutoWidth(true);
        gridReclutas.addColumn(Recluta::getApellido).setHeader("Apellido").setAutoWidth(true);
        gridReclutas.addColumn(Recluta::getAltura).setHeader("Altura").setAutoWidth(true);
        gridReclutas.addColumn(Recluta::getPeso).setHeader("Peso").setAutoWidth(true);
        gridReclutas.addColumn(r -> r.getIdRama() != null ? r.getIdRama().getNombre() : "")
                .setHeader("Rama").setAutoWidth(true);
        gridReclutas.setWidthFull();

        Button btnBuscar = new Button("Buscar soldados", e -> {
            List<Recluta> resultados;
            if (divisionEspecialCombo.getValue() != null) {
                resultados = crudService.listarReclutasPorDivisionEspecial(
                        divisionEspecialCombo.getValue().getIdDivisionEspecial());
            } else if (divisionCombo.getValue() != null) {
                if (Boolean.TRUE.equals(soloAptosField.getValue())) {
                    resultados = crudService.listarReclutasAptosPorDivision(
                            divisionCombo.getValue().getIdDivision());
                } else {
                    resultados = crudService.listarReclutasPorDivision(
                            divisionCombo.getValue().getIdDivision());
                }
            } else {
                Notification.show("Seleccione una división o una división especial");
                return;
            }
            gridReclutas.setItems(resultados);
            Notification.show(resultados.isEmpty()
                    ? "No se encontraron soldados adscritos"
                    : "Soldados encontrados: " + resultados.size());
        });
        btnBuscar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnLimpiar = new Button("Limpiar", e -> {
            limpiar(divisionCombo, divisionEspecialCombo, soloAptosField);
            soloAptosField.setEnabled(true);
            gridReclutas.setItems(List.of());
        });

        HorizontalLayout filtros = new HorizontalLayout(divisionCombo, divisionEspecialCombo, soloAptosField,
                btnBuscar, btnLimpiar);
        filtros.setWidthFull();
        filtros.getStyle().set("flex-wrap", "wrap");

        ComboBox<Division> divisionDetalleCombo = crearCombo("División", Division.class, this::etiquetaDivision);

        Grid<AptoDivisionEspecial> gridAdscripciones = new Grid<>(AptoDivisionEspecial.class, false);
        gridAdscripciones.addColumn(a -> a.getIdRecluta() != null ? nombreCompleto(a.getIdRecluta()) : "")
                .setHeader("Recluta").setAutoWidth(true);
        gridAdscripciones.addColumn(a -> a.getIdRecluta() != null && a.getIdRecluta().getIdRama() != null
                ? a.getIdRecluta().getIdRama().getNombre()
                : "")
                .setHeader("Rama").setAutoWidth(true);
        gridAdscripciones.addColumn(a -> a.getIdDivisionEspecial() != null
                ? a.getIdDivisionEspecial().getNombre()
                : "")
                .setHeader("División Especial").setAutoWidth(true);
        gridAdscripciones.addColumn(a -> a.getIdDivisionEspecial() != null
                && a.getIdDivisionEspecial().getIdDivision() != null
                        ? a.getIdDivisionEspecial().getIdDivision().getNombre()
                        : "")
                .setHeader("División").setAutoWidth(true);
        gridAdscripciones.addColumn(a -> Boolean.TRUE.equals(a.getAptoServicio()) ? "Sí" : "No")
                .setHeader("Apto Servicio").setAutoWidth(true);
        gridAdscripciones.addColumn(AptoDivisionEspecial::getFechaEvaluacion)
                .setHeader("Fecha Evaluación").setAutoWidth(true);
        gridAdscripciones.addColumn(AptoDivisionEspecial::getPuntaje).setHeader("Puntaje").setAutoWidth(true);
        gridAdscripciones.setWidthFull();

        Button btnVerAdscripciones = new Button("Ver adscripciones", e -> {
            Division division = divisionDetalleCombo.getValue();
            if (division == null) {
                Notification.show("Seleccione una división");
                return;
            }
            List<AptoDivisionEspecial> adscripciones = crudService.listarAptosPorDivision(
                    division.getIdDivision());
            gridAdscripciones.setItems(adscripciones);
            Notification.show(adscripciones.isEmpty()
                    ? "No hay adscripciones para la división"
                    : "Adscripciones encontradas: " + adscripciones.size());
        });
        btnVerAdscripciones.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnLimpiarDetalle = new Button("Limpiar", e -> {
            divisionDetalleCombo.clear();
            gridAdscripciones.setItems(List.of());
        });

        HorizontalLayout filtrosDetalle = new HorizontalLayout(divisionDetalleCombo, btnVerAdscripciones,
                btnLimpiarDetalle);
        filtrosDetalle.setWidthFull();
        filtrosDetalle.getStyle().set("flex-wrap", "wrap");

        layout.add(new H3("Soldados adscritos a una división"), filtros, gridReclutas,
                new H3("Adscripciones detalladas"), filtrosDetalle, gridAdscripciones);
        return layout;
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================
    private <T> ComboBox<T> crearCombo(String label, Class<T> clase, ItemLabelGenerator<T> etiqueta) {
        ComboBox<T> combo = new ComboBox<>(label);
        List<T> items = new ArrayList<>(crudService.listarTodos(clase));
        ListDataProvider<T> provider = new ListDataProvider<>(items);
        combo.setItems(provider);
        combo.setItemLabelGenerator(etiqueta);
        combo.setWidthFull();
        comboRefrescos.add(() -> {
            items.clear();
            items.addAll(crudService.listarTodos(clase));
            provider.refreshAll();
        });
        return combo;
    }

    private void recargarCombos() {
        comboRefrescos.forEach(SerializableRunnable::run);
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String etiquetaRama(Rama rama) {
        if (rama == null) {
            return "";
        }
        return rama.getNombre() != null ? rama.getNombre() : "Rama " + rama.getIdRama();
    }

    private String etiquetaDivision(Division division) {
        if (division == null) {
            return "";
        }
        String nombre = division.getNombre() != null ? division.getNombre() : "División " + division.getIdDivision();
        String rama = division.getIdRama() != null ? division.getIdRama().getNombre() : "sin rama";
        return nombre + " (" + rama + ")";
    }

    private String etiquetaDivisionEspecial(DivisionEspecial divisionEspecial) {
        if (divisionEspecial == null) {
            return "";
        }
        String nombre = divisionEspecial.getNombre() != null ? divisionEspecial.getNombre()
                : "División especial " + divisionEspecial.getIdDivisionEspecial();
        String division = divisionEspecial.getIdDivision() != null ? divisionEspecial.getIdDivision().getNombre()
                : "sin división";
        return nombre + " (" + division + ")";
    }

    private String nombreCompleto(Recluta recluta) {
        if (recluta == null) {
            return "";
        }
        String nombre = recluta.getNombre() != null ? recluta.getNombre() : "";
        String apellido = recluta.getApellido() != null ? recluta.getApellido() : "";
        return (nombre + " " + apellido).trim();
    }

    private void limpiar(HasValue<?, ?>... campos) {
        for (HasValue<?, ?> campo : campos) {
            campo.clear();
        }
    }

    private HorizontalLayout crearAcciones(Button... botones) {
        HorizontalLayout layout = new HorizontalLayout(botones);
        layout.getStyle().set("flex-wrap", "wrap");
        return layout;
    }
}   