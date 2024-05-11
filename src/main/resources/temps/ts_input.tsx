import React, { JSX } from 'react';


type Props = {
    label?: string | undefined,
    topRightLabel?: string | undefined,
    bottomLeftLabel?: string | undefined,
    bottomRightLabel?: string | undefined,
    error?: string | undefined

}
const InputWrapper: React.FC<React.PropsWithChildren<Props>> = ({
                                                                    label,
                                                                    topRightLabel,
                                                                    bottomLeftLabel,
                                                                    bottomRightLabel,
                                                                    children,
                                                                }) => {


    const getTopLabel = (): JSX.Element | undefined => {
        if (!label && !topRightLabel) {
            return undefined;
        }
        return <div className="label">
            {label && <span className="label-text">{label}</span>}
            {topRightLabel && <span className="label-text-alt">{topRightLabel}</span>}
        </div>;
    };


    const getBottomLabel = (): JSX.Element | undefined => {
        if (!bottomLeftLabel && !bottomRightLabel) {
            return undefined;
        }
        return <div className="label">
            {bottomLeftLabel && <span className="label-text">{bottomLeftLabel}</span>}
            {bottomRightLabel && <span className="label-text-alt">{bottomRightLabel}</span>}
        </div>;
    };

    let top: React.JSX.Element | undefined = getTopLabel();
    let bottom: React.JSX.Element | undefined = getBottomLabel();

    return <label className="form-control w-full">
        {top && top}
        {children}
        {bottom && bottom}
    </label>;
};

export function get_input_class(error: string | undefined) : string {
    return error ? 'input w-full input-error input-bordered ' : 'input w-full input-bordered'
}

export function get_textarea_class(error: string | undefined) : string {
    return error ? 'textarea w-full textarea-error textarea-bordered ' : 'textarea w-full textarea-bordered'
}

export {
    InputWrapper,
};